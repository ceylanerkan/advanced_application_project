package com.ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// Assuming you have this exception defined; otherwise, use ResponseStatusException
import com.ecommerce.exception.ResourceNotFoundException; 
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.StoreRepository;
import com.ecommerce.model.Store;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    private void populateItemCounts(List<Order> orders) {
        for (Order order : orders) {
            List<com.ecommerce.model.OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
            order.setItemCount(items.size());
            if (!items.isEmpty()) {
                String first = items.get(0).getProduct() != null ? items.get(0).getProduct().getName() : "Item";
                order.setItemSummary(items.size() > 1 ? first + " +" + (items.size() - 1) + " more" : first);
            } else {
                order.setItemSummary("—");
            }
        }
    }

    // Helper method to fetch the current user via email
    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public Page<Order> getOrdersPaged(String email, int page, int size) {
        User currentUser = getAuthenticatedUser(email);
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Order> result;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            result = orderRepository.findAll(pageRequest);
        } else if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) {
            List<Store> stores = storeRepository.findByOwner_Id(currentUser.getId());
            List<Order> all = new java.util.ArrayList<>();
            for (Store s : stores) {
                all.addAll(orderRepository.findByStoreId(s.getId()));
            }
            int start = (int) pageRequest.getOffset();
            int end = Math.min(start + pageRequest.getPageSize(), all.size());
            List<Order> slice = start >= all.size() ? new java.util.ArrayList<>() : all.subList(start, end);
            result = new PageImpl<>(slice, pageRequest, all.size());
        } else {
            result = orderRepository.findByUser_Email(currentUser.getEmail(), pageRequest);
        }
        populateItemCounts(result.getContent());
        return result;
    }

    public List<Order> getAllOrders(String email) {
        User currentUser = getAuthenticatedUser(email);

        List<Order> orders;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            orders = orderRepository.findAll();
        } else if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) {
            List<Store> stores = storeRepository.findByOwner_Id(currentUser.getId());
            orders = new java.util.ArrayList<>();
            for (Store s : stores) {
                orders.addAll(orderRepository.findByStoreId(s.getId()));
            }
        } else {
            orders = orderRepository.findByUser_Email(currentUser.getEmail());
        }
        populateItemCounts(orders);
        return orders;
    }

    public Order getOrderById(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        User currentUser = getAuthenticatedUser(email);

        // Security Check: Verify Ownership (Mitigates AV-05)
        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            // Check if the order's user ID matches the logged-in user's database ID
            if (!order.getUser().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You do not own this order.");
            }
        }
        // ADMIN and CORPORATE pass through automatically

        return order;
    }

    public Order createOrder(Order order, String email) {
        User currentUser = getAuthenticatedUser(email);
        order.setUser(currentUser);

        if (order.getStore() == null || order.getStore().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Store ID is required");
        }
        Store store = storeRepository.findById(order.getStore().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        order.setStore(store);
        order.setCreatedAt(LocalDateTime.now().toString());

        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails, String email) {
        // This utilizes our secure getOrderById, enforcing RBAC automatically.
        Order order = getOrderById(id, email);
        User currentUser = getAuthenticatedUser(email);

        // Apply safe updates — only overwrite non-null incoming values so that
        // partial updates (e.g. status-only from order fulfillment) don't wipe existing data.
        order.setStatus(orderDetails.getStatus());
        if (orderDetails.getGrandTotal() != null) order.setGrandTotal(orderDetails.getGrandTotal());
        if (orderDetails.getBaseCurrency() != null) order.setBaseCurrency(orderDetails.getBaseCurrency());
        if (orderDetails.getOriginalCurrency() != null) order.setOriginalCurrency(orderDetails.getOriginalCurrency());
        if (orderDetails.getExchangeRate() != null) order.setExchangeRate(orderDetails.getExchangeRate());
        // createdAt is intentionally not updated — preserve original timestamp
        
        // Security Check: Only Admins can reassign an order to a different user or store
        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            order.setUser(orderDetails.getUser());
            order.setStore(orderDetails.getStore());
        }

        return orderRepository.save(order);
    }

    public void deleteOrder(Long id, String email) {
        // Fetch the order securely first
        Order order = getOrderById(id, email);
        User currentUser = getAuthenticatedUser(email);
        
        // Prevent individuals from deleting processed orders
        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individuals cannot delete system orders.");
        }

        orderRepository.delete(order);
    }
}
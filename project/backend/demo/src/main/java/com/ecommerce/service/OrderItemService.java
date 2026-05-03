package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<OrderItem> getAllOrderItems(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all order items");
        }
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItemById(Long id, String email) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + id));

        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Security Check: Verify ownership via the parent order's user
        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            if (!orderItem.getOrder().getUser().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Order item does not belong to you");
            }
        }
        return orderItem;
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrder_Id(orderId);
    }

    public OrderItem createOrderItem(OrderItem orderItem, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (orderItem.getOrder() == null || orderItem.getOrder().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required");
        }
        Order order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (orderItem.getProduct() == null || orderItem.getProduct().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        Product product = productRepository.findById(orderItem.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            if (!order.getUser().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only add items to your own orders");
            }
        }

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        return orderItemRepository.save(orderItem);
    }

    public OrderItem updateOrderItem(Long id, OrderItem orderItemDetails, String email) {
        OrderItem orderItem = getOrderItemById(id, email); // Inherits ownership check
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individuals cannot update order items directly.");
        }

        orderItem.setQuantity(orderItemDetails.getQuantity());
        orderItem.setPrice(orderItemDetails.getPrice());
        orderItem.setBaseCurrency(orderItemDetails.getBaseCurrency());
        orderItem.setOriginalCurrency(orderItemDetails.getOriginalCurrency());
        orderItem.setExchangeRate(orderItemDetails.getExchangeRate());
        orderItem.setProduct(orderItemDetails.getProduct());

        return orderItemRepository.save(orderItem);
    }

    public void deleteOrderItem(Long id, String email) {
        OrderItem orderItem = getOrderItemById(id, email); // Inherits ownership check
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individuals cannot delete order items.");
        }
        orderItemRepository.delete(orderItem);
    }
}

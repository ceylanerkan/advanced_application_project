package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// Assuming you have this exception defined; otherwise, use ResponseStatusException
import com.ecommerce.exception.ResourceNotFoundException; 
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // Helper method to fetch the current user via email
    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public List<Order> getAllOrders(String email) {
        User currentUser = getAuthenticatedUser(email);

        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            return orderRepository.findAll();
        } else if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) {
            return orderRepository.findAll(); // Corporate users see all orders for now
        } else {
            // INDIVIDUAL user
            return orderRepository.findByUser_Email(currentUser.getEmail());
        }
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

        // Security Check: Prevent Mass Assignment (AV-11)
        // Force the order to belong to the person creating it.
        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            order.setUser(currentUser); 
        }

        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails, String email) {
        // This utilizes our secure getOrderById, enforcing RBAC automatically.
        Order order = getOrderById(id, email);
        User currentUser = getAuthenticatedUser(email);

        // Apply safe updates
        order.setStatus(orderDetails.getStatus());
        order.setGrandTotal(orderDetails.getGrandTotal());
        order.setBaseCurrency(orderDetails.getBaseCurrency());
        order.setOriginalCurrency(orderDetails.getOriginalCurrency());
        order.setExchangeRate(orderDetails.getExchangeRate());
        order.setCreatedAt(orderDetails.getCreatedAt());
        
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
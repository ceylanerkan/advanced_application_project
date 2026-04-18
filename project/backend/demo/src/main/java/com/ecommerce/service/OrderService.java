package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Order order = getOrderById(id);
        
        order.setUser(orderDetails.getUser());
        order.setStatus(orderDetails.getStatus());
        order.setGrandTotal(orderDetails.getGrandTotal());
        order.setStoreId(orderDetails.getStoreId());
        
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        // Fetch the order first to ensure a 404 is thrown if it does not exist
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}
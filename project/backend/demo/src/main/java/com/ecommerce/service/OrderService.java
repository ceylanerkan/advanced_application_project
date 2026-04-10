package com.ecommerce.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public void getUserSpecificOrders() {
        // 1. Get the currently authenticated user's email from the JWT context
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Query the database ONLY for orders belonging to this email
        // orderRepository.findByUserEmail(currentUserEmail);
    }
}
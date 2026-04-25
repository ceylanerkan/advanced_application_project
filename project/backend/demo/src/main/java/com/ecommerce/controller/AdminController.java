package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
// Failsafe: Ensures every single endpoint in this controller requires the ADMIN role
@PreAuthorize("hasRole('ADMIN')") 
public class AdminController {

    // Section 4.3: User Management
    @GetMapping("/users")
    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok("Admin Access: Fetching all users across the platform");
    }

    // Section 4.3: Store Approval and Management
    @PostMapping("/stores/{storeId}/approve")
    public ResponseEntity<String> approveStore(@PathVariable Long storeId) {
        return ResponseEntity.ok("Admin Access: Store " + storeId + " approved.");
    }
    
    // Section 4.3: Platform-wide analytics
    @GetMapping("/analytics/platform")
    public ResponseEntity<String> getPlatformAnalytics() {
        return ResponseEntity.ok("Admin Access: Full platform analytics dashboard data.");
    }
}
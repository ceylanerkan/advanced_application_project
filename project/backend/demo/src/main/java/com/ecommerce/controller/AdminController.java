package com.ecommerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.Order;
import com.ecommerce.model.Store;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.StoreService;
import com.ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// Failsafe: Ensures every single endpoint in this controller requires the ADMIN role
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Panel", description = "Admin-only endpoints for platform-wide user, store, and order management")
public class AdminController {

    private final UserService userService;
    private final StoreService storeService;
    private final OrderService orderService;

    // ─── User Management ─────────────────────────────────────────────

    @Operation(summary = "Get all users", description = "Retrieves a complete list of all registered users across the platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(Authentication authentication) {
        return ResponseEntity.ok(userService.getAllUsers(authentication.getName()));
    }

    @Operation(summary = "Delete any user", description = "Admin-only endpoint to delete any user account by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, Authentication authentication) {
        userService.deleteUser(userId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ─── Store Management ────────────────────────────────────────────

    @Operation(summary = "Get all stores", description = "Retrieves all stores across the platform for admin review.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stores retrieved successfully")
    })
    @GetMapping("/stores")
    public ResponseEntity<List<Store>> getAllStores(Authentication authentication) {
        return ResponseEntity.ok(storeService.getAllStores(authentication.getName()));
    }

    @Operation(summary = "Delete a store", description = "Admin-only endpoint to remove a store from the platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long storeId, Authentication authentication) {
        storeService.deleteStore(storeId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ─── Order Management ────────────────────────────────────────────

    @Operation(summary = "Get all orders", description = "Retrieves all orders across the platform for admin oversight.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getAllOrders(authentication.getName()));
    }

    @Operation(summary = "Update order status", description = "Admin-only endpoint to update any order's details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long orderId, @org.springframework.web.bind.annotation.RequestBody Order orderDetails, Authentication authentication) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, orderDetails, authentication.getName()));
    }

    @Operation(summary = "Delete an order", description = "Admin-only endpoint to delete any order from the platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId, Authentication authentication) {
        orderService.deleteOrder(orderId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
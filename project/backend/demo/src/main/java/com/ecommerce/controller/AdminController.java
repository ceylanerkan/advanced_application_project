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
import com.ecommerce.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;

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

    // ─── Dashboard Management ────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<java.util.Map<String, Object>> getAdminDashboard(Authentication authentication) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        List<Order> orders = orderService.getAllOrders(authentication.getName());
        
        // Let's use orderRepository from orderService to calculate this
        // Actually since we don't have orderRepository directly, we'll just process the orders list:
        double[] monthlyRevenue = new double[7];
        String[] monthLabels = new String[7];
        java.time.LocalDate now = java.time.LocalDate.now();
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate monthDate = now.minusMonths(6 - i);
            monthLabels[i] = monthDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM"));
            final int m = monthDate.getMonthValue();
            final int y = monthDate.getYear();
            monthlyRevenue[i] = orders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    try {
                        java.time.LocalDate d = java.time.LocalDate.parse(o.getCreatedAt().substring(0, 10));
                        return d.getMonthValue() == m && d.getYear() == y;
                    } catch(Exception e) { return false; }
                })
                .mapToDouble(o -> o.getGrandTotal() != null ? o.getGrandTotal() : 0.0)
                .sum();
        }
        
        java.util.List<Double> monthlyRevList = new java.util.ArrayList<>();
        for(double d : monthlyRevenue) monthlyRevList.add(d);

        java.util.Map<String, Integer> regionTotals = new java.util.HashMap<>();
        for (Order o : orders) {
            String city = o.getShippingCity() != null ? o.getShippingCity() : "Unknown";
            // Map city to a broader region for the admin dashboard (demo purposes)
            String region = "Others";
            if (city.equalsIgnoreCase("New York") || city.equalsIgnoreCase("Chicago") || city.equalsIgnoreCase("Houston") || city.equalsIgnoreCase("Los Angeles") || city.equalsIgnoreCase("Phoenix")) {
                region = "North America";
            } else if (city.equalsIgnoreCase("London") || city.equalsIgnoreCase("Paris") || city.equalsIgnoreCase("Berlin")) {
                region = "Europe";
            } else if (city.equalsIgnoreCase("Tokyo") || city.equalsIgnoreCase("Beijing") || city.equalsIgnoreCase("Seoul")) {
                region = "Asia";
            }
            regionTotals.put(region, regionTotals.getOrDefault(region, 0) + 1);
        }

        java.util.List<String> regionLabels = new java.util.ArrayList<>(regionTotals.keySet());
        java.util.List<Integer> regionValues = new java.util.ArrayList<>(regionTotals.values());
        if (regionLabels.isEmpty()) {
            regionLabels = java.util.Arrays.asList("North America", "Europe", "Asia", "Others");
            regionValues = java.util.Arrays.asList(0, 0, 0, 0);
        }

        Double totalRevenueRaw = orderItemRepository.sumTotalRevenue();
        double totalRevenueStr = totalRevenueRaw != null ? totalRevenueRaw : 0.0;
        long totalOrders = orders.size();
        long totalUsers = userService.getAllUsers(authentication.getName()).size();
        long activeStores = storeService.getAllStores(authentication.getName()).stream()
                .filter(s -> s.getStatus() != null && !"suspended".equalsIgnoreCase(s.getStatus()) && !"inactive".equalsIgnoreCase(s.getStatus()))
                .count();

        response.put("monthLabels", java.util.Arrays.asList(monthLabels));
        response.put("monthValues", monthlyRevList);
        response.put("regionLabels", regionLabels);
        response.put("regionValues", regionValues);
        
        response.put("totalUsers", totalUsers);
        response.put("totalRevenue", totalRevenueStr);
        response.put("activeStores", activeStores);
        response.put("totalOrders", totalOrders);

        return ResponseEntity.ok(response);
    }
}
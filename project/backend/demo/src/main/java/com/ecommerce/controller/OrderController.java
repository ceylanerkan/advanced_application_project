package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for processing and tracking customer orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Get all orders", description = "Retrieves orders. Individuals see their own, Corporates see their store's, Admins see all.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(Authentication authentication) {
        // authentication.getName() returns the email based on your UserDetails implementation
        return ResponseEntity.ok(orderService.getAllOrders(authentication.getName()));
    }

    @Operation(summary = "Get an order by ID", description = "Retrieves the details of a specific order. Users can only access orders they own or manage.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own this order"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrderById(id, authentication.getName()));
    }

    @Operation(summary = "Create a new order", description = "Creates a new order record securely linked to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully")
    })
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order, Authentication authentication) {
        return new ResponseEntity<>(orderService.createOrder(order, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing order", description = "Updates an order by its ID. Users can only update orders they are authorized to manage.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Unauthorized to modify this order")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order orderDetails, Authentication authentication) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderDetails, authentication.getName()));
    }

    @Operation(summary = "Delete an order", description = "Deletes a specific order by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Unauthorized to delete this order")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id, Authentication authentication) {
        orderService.deleteOrder(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
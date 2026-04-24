package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.OrderItem;
import com.ecommerce.service.OrderItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Item Management", description = "Endpoints for managing individual line items within orders")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "Get all order items (ADMIN only)", description = "Retrieves all order items across the platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order items retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only admins can list all order items")
    })
    @GetMapping
    public ResponseEntity<List<OrderItem>> getAllOrderItems(Authentication authentication) {
        return ResponseEntity.ok(orderItemService.getAllOrderItems(authentication.getName()));
    }

    @Operation(summary = "Get an order item by ID", description = "Retrieves a specific order item. Individuals can only view items from their own orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order item retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Order item does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(id, authentication.getName()));
    }

    @Operation(summary = "Get order items by order ID", description = "Retrieves all items belonging to a specific order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order items retrieved successfully")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.getOrderItemsByOrderId(orderId));
    }

    @Operation(summary = "Create a new order item", description = "Adds an item to an order. Individuals can only add to their own orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order item created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot add items to another user's order")
    })
    @PostMapping
    public ResponseEntity<OrderItem> createOrderItem(@Valid @RequestBody OrderItem orderItem, Authentication authentication) {
        return new ResponseEntity<>(orderItemService.createOrderItem(orderItem, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an order item (Admin/Corporate)", description = "Updates an order item. Individuals cannot update order items directly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order item updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individuals cannot update order items")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItem orderItemDetails, Authentication authentication) {
        return ResponseEntity.ok(orderItemService.updateOrderItem(id, orderItemDetails, authentication.getName()));
    }

    @Operation(summary = "Delete an order item (Admin/Corporate)", description = "Deletes an order item. Individuals cannot delete order items.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order item deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individuals cannot delete order items")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id, Authentication authentication) {
        orderItemService.deleteOrderItem(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

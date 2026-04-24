package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.Store;
import com.ecommerce.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "Endpoints for managing stores (Corporate and Admin)")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "Get all stores", description = "Admins see all stores; Corporate users see only their own. Individual users are denied.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stores retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individual users cannot view stores")
    })
    @GetMapping
    public ResponseEntity<List<Store>> getAllStores(Authentication authentication) {
        return ResponseEntity.ok(storeService.getAllStores(authentication.getName()));
    }

    @Operation(summary = "Get a store by ID", description = "Retrieves a specific store. Corporate users can only view their own stores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Store does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(storeService.getStoreById(id, authentication.getName()));
    }

    @Operation(summary = "Create a new store", description = "Creates a new store. Corporate users are automatically set as the owner. Individuals are denied.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Store created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individual users cannot create stores")
    })
    @PostMapping
    public ResponseEntity<Store> createStore(@Valid @RequestBody Store store, Authentication authentication) {
        return new ResponseEntity<>(storeService.createStore(store, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a store", description = "Updates store details. Only Admins can reassign ownership.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Unauthorized to update this store")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Long id, @Valid @RequestBody Store storeDetails, Authentication authentication) {
        return ResponseEntity.ok(storeService.updateStore(id, storeDetails, authentication.getName()));
    }

    @Operation(summary = "Delete a store (Admin only)", description = "Deletes a store. Only Admins can perform this action.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only Admins can delete stores")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id, Authentication authentication) {
        storeService.deleteStore(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

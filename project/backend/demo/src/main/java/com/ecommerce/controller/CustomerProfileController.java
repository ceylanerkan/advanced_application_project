package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.CustomerProfile;
import com.ecommerce.service.CustomerProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer-profiles")
@RequiredArgsConstructor
@Tag(name = "Customer Profile Management", description = "Endpoints for managing customer profile data (age, city, membership)")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @Operation(summary = "Get all customer profiles (ADMIN only)", description = "Retrieves all customer profiles. Restricted to ADMIN users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only admins can list all profiles")
    })
    @GetMapping
    public ResponseEntity<List<CustomerProfile>> getAllCustomerProfiles(Authentication authentication) {
        return ResponseEntity.ok(customerProfileService.getAllCustomerProfiles(authentication.getName()));
    }

    @Operation(summary = "Get a customer profile by ID", description = "Retrieves a specific customer profile. Users can only view their own unless they are an ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot view another user's profile"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerProfile> getCustomerProfileById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(customerProfileService.getCustomerProfileById(id, authentication.getName()));
    }

    @Operation(summary = "Create a customer profile", description = "Creates a new customer profile linked to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully")
    })
    @PostMapping
    public ResponseEntity<CustomerProfile> createCustomerProfile(@Valid @RequestBody CustomerProfile profile, Authentication authentication) {
        return new ResponseEntity<>(customerProfileService.createCustomerProfile(profile, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a customer profile", description = "Updates an existing customer profile. Users can only update their own unless they are an ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot modify another user's profile")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerProfile> updateCustomerProfile(@PathVariable Long id, @Valid @RequestBody CustomerProfile profileDetails, Authentication authentication) {
        return ResponseEntity.ok(customerProfileService.updateCustomerProfile(id, profileDetails, authentication.getName()));
    }

    @Operation(summary = "Delete a customer profile", description = "Deletes a customer profile. Users can delete their own, Admins can delete anyone's.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete another user's profile")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerProfile(@PathVariable Long id, Authentication authentication) {
        customerProfileService.deleteCustomerProfile(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

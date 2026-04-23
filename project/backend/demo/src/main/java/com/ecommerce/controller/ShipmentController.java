package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.Shipment;
import com.ecommerce.service.ShipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipment Management", description = "Endpoints for tracking and managing order shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Operation(summary = "Get all shipments", description = "Returns shipments related to the authenticated user.")
    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments(Authentication authentication) {
        return ResponseEntity.ok(shipmentService.getAllShipments(authentication.getName()));
    }

    @Operation(summary = "Get a shipment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(shipmentService.getShipmentByIdSecurely(id, authentication.getName()));
    }

    @Operation(summary = "Create a new shipment (Admin/Corporate)")
    @PostMapping
    public ResponseEntity<Shipment> createShipment(@Valid @RequestBody Shipment shipment, Authentication authentication) {
        return new ResponseEntity<>(shipmentService.createShipment(shipment, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing shipment (Admin/Corporate)")
    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(@PathVariable Long id, @Valid @RequestBody Shipment shipmentDetails, Authentication authentication) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, shipmentDetails, authentication.getName()));
    }

    @Operation(summary = "Delete a shipment (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id, Authentication authentication) {
        shipmentService.deleteShipment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
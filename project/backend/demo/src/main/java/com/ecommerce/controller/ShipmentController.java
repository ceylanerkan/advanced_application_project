package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "Get all shipments", description = "Retrieves a complete list of all shipments.")
    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @Operation(summary = "Get a shipment by ID", description = "Retrieves the details of a specific shipment.")
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    @Operation(summary = "Create a new shipment", description = "Creates a new shipment record. Validates required fields before saving.")
    @PostMapping
    public ResponseEntity<Shipment> createShipment(@Valid @RequestBody Shipment shipment) {
        return new ResponseEntity<>(shipmentService.createShipment(shipment), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing shipment", description = "Updates a shipment by its ID. Validates required fields.")
    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(@PathVariable Long id, @Valid @RequestBody Shipment shipmentDetails) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, shipmentDetails));
    }

    @Operation(summary = "Delete a shipment", description = "Deletes a specific shipment by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}
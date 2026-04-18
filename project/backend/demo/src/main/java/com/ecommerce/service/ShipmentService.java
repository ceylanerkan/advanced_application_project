package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Shipment;
import com.ecommerce.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
    }

    public Shipment createShipment(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    public Shipment updateShipment(Long id, Shipment shipmentDetails) {
        Shipment shipment = getShipmentById(id);
        
        // Update fields
        shipment.setOrder(shipmentDetails.getOrder());
        shipment.setWarehouse(shipmentDetails.getWarehouse());
        shipment.setMode(shipmentDetails.getMode());
        shipment.setStatus(shipmentDetails.getStatus());
        
        return shipmentRepository.save(shipment);
    }

    public void deleteShipment(Long id) {
        // Optional: Check if it exists before deleting to throw the 404 properly
        Shipment shipment = getShipmentById(id);
        shipmentRepository.delete(shipment);
    }
}
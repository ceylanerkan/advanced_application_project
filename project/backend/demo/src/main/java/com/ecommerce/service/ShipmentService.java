package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Shipment;
import com.ecommerce.model.User;
import com.ecommerce.repository.ShipmentRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public List<Shipment> getAllShipments(String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            return shipmentRepository.findAll();
        }
        // If they aren't admin, ideally they query by userId or storeId via custom Repository methods
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Need custom query method to fetch user specific shipments");
    }

    public Shipment getShipmentByIdSecurely(Long id, String email) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Mitigate AV-05: Ensure shipment's order belongs to user
        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            if (!shipment.getOrder().getUser().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Shipment does not belong to you");
            }
        }
        return shipment;
    }

    public Shipment createShipment(Shipment shipment, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individuals cannot create shipments.");
        }
        return shipmentRepository.save(shipment);
    }

    public Shipment updateShipment(Long id, Shipment shipmentDetails, String email) {
        Shipment shipment = getShipmentByIdSecurely(id, email);
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individuals cannot update shipments.");
        }
        
        shipment.setWarehouse(shipmentDetails.getWarehouse());
        shipment.setMode(shipmentDetails.getMode());
        shipment.setStatus(shipmentDetails.getStatus());
        
        return shipmentRepository.save(shipment);
    }

    public void deleteShipment(Long id, String email) {
        Shipment shipment = getShipmentByIdSecurely(id, email);
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Admins can delete shipments.");
        }
        shipmentRepository.delete(shipment);
    }
}
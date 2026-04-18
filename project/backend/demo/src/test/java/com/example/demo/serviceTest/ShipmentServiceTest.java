package com.example.demo.serviceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.model.Shipment;
import com.ecommerce.repository.ShipmentRepository;
import com.ecommerce.service.ShipmentService;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setWarehouse("Block A");
        testShipment.setStatus("In Transit");
    }

    @Test
    void getAllShipments_ShouldReturnList() {
        when(shipmentRepository.findAll()).thenReturn(Arrays.asList(testShipment));
        List<Shipment> shipments = shipmentService.getAllShipments();
        assertEquals(1, shipments.size());
    }

    @Test
    void getShipmentById_ShouldReturnShipment() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        Shipment found = shipmentService.getShipmentById(1L);
        assertEquals("In Transit", found.getStatus());
    }
}
package com.ecommerce.controller;

import com.ecommerce.model.Shipment;
import com.ecommerce.security.service.JwtService;
import com.ecommerce.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;
    
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ShipmentService shipmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setWarehouse("Block A");
        testShipment.setStatus("In Transit");
    }

    @Test
    void getAllShipments_ShouldReturn200() throws Exception {
        Mockito.when(shipmentService.getAllShipments()).thenReturn(List.of(testShipment));
        
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].warehouse").value("Block A"));
    }
}
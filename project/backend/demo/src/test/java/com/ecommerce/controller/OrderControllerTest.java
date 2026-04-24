package com.ecommerce.controller;

import com.ecommerce.security.service.JwtService;

import org.springframework.context.annotation.Import;

import com.ecommerce.controller.OrderController;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @MockitoBean
    private UserDetailsService userDetailsService;
    
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setStatus("Completed");
        testOrder.setGrandTotal(150.50);
    }

    @Test
    void getAllOrders_ShouldReturn200() throws Exception {
        Mockito.when(orderService.getAllOrders(Mockito.anyString())).thenReturn(Arrays.asList(testOrder));
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("Completed"));
    }
}
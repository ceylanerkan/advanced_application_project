package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.security.service.JwtService;
import com.ecommerce.service.UserService;
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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;
    
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setCustomerId("A123");
        testUser.setRoleType("INDIVIDUAL");
    }

    @Test
    void getAllUsers_ShouldReturn200() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(testUser));
        
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("A123"));
    }
}
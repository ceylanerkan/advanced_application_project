package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false) // Güvenlik filtrelerini kapatır
@SpringBootTest
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;

    private String adminId;
    private String normalId;

    private String adminToken;
    private String normalToken;

    @BeforeEach
    void setUp() {
        adminId = "admin_" + UUID.randomUUID().toString();
        adminUser = new User();
        adminUser.setCustomerId(adminId);
        adminUser.setPassword("password");
        adminUser.setRoleType("ADMIN");
        adminUser = userRepository.save(adminUser);

        normalId = "user_" + UUID.randomUUID().toString();
        normalUser = new User();
        normalUser.setCustomerId(normalId);
        normalUser.setPassword("password");
        normalUser.setRoleType("INDIVIDUAL");
        normalUser = userRepository.save(normalUser);

        adminToken = jwtService.generateToken(adminUser);
        normalToken = jwtService.generateToken(normalUser);
    }

    @Test
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].customerId", hasItems(adminId, normalId)));
    }

    @Test
    void getAllUsers_AsNormalUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + normalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_AsAdmin_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", normalUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(normalId));
    }

    @Test
    void getUserById_AsNormalUserForOwnProfile_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", normalUser.getId())
                .header("Authorization", "Bearer " + normalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(normalId));
    }

    @Test
    @Disabled
    void getUserById_AsNormalUserForOtherProfile_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/{id}", adminUser.getId())
                .header("Authorization", "Bearer " + normalToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        User newUser = new User();
        newUser.setCustomerId("new_" + UUID.randomUUID().toString());
        newUser.setPassword("password");
        newUser.setRoleType("INDIVIDUAL"); 

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(newUser.getCustomerId()))
                .andExpect(jsonPath("$.roleType").value("INDIVIDUAL"));
    }

    @Test
    void updateUser_AsAdmin_ShouldUpdateRole() throws Exception {
        User updateDetails = new User();
        updateDetails.setCustomerId(normalId);
        updateDetails.setPassword("new_password");
        updateDetails.setRoleType("ADMIN"); // Admin can change role

        mockMvc.perform(put("/api/users/{id}", normalUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(normalId))
                .andExpect(jsonPath("$.roleType").value("ADMIN"));
    }

    @Test
    void updateUser_AsNormalUser_ShouldNotUpdateRole() throws Exception {
        User updateDetails = new User();
        updateDetails.setCustomerId(normalId);
        updateDetails.setPassword("new_password");
        updateDetails.setRoleType("ADMIN"); // Attempt to escalate role, should be ignored by the service

        mockMvc.perform(put("/api/users/{id}", normalUser.getId())
                .header("Authorization", "Bearer " + normalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(normalId))
                .andExpect(jsonPath("$.roleType").value("INDIVIDUAL")); // Role remains unchanged
    }

    @Test
    void deleteUser_AsAdmin_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", normalUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", normalUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}

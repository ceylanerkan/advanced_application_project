package com.example.demo;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.security.config.JwtProperties;
import com.ecommerce.security.service.JwtService;
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails dummyUser;

    @BeforeEach
    void setUp() {
        // 1. Manually create our JwtProperties record for testing
        // Using the same 256-bit hex key you have in your application.properties
        String testSecretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        long testExpiration = 900000; // 15 minutes
        JwtProperties.RefreshToken testRefreshToken = new JwtProperties.RefreshToken(604800000); // 7 days
        
        JwtProperties jwtProperties = new JwtProperties(testSecretKey, testExpiration, testRefreshToken);

        // 2. Instantiate the service with the properties
        jwtService = new JwtService(jwtProperties);

        // 3. Create a dummy Spring Security UserDetails object
        dummyUser = new User("testuser@example.com", "password", new ArrayList<>());
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtService.generateToken(dummyUser);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(dummyUser);
        String extractedUsername = jwtService.extractUsername(token);
        
        assertEquals(dummyUser.getUsername(), extractedUsername);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForCorrectUserAndUnexpiredToken() {
        String token = jwtService.generateToken(dummyUser);
        
        boolean isValid = jwtService.isTokenValid(token, dummyUser);
        
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForDifferentUser() {
        String token = jwtService.generateToken(dummyUser);
        
        // Create a different user
        UserDetails wrongUser = new User("wronguser@example.com", "password", new ArrayList<>());
        
        boolean isValid = jwtService.isTokenValid(token, wrongUser);
        
        assertFalse(isValid);
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String refreshToken = jwtService.generateRefreshToken(dummyUser);
        
        assertNotNull(refreshToken);
        assertEquals(dummyUser.getUsername(), jwtService.extractUsername(refreshToken));
    }
}
package com.example.demo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;   
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.config.ApplicationConfig;

// This tells JUnit to enable Mockito features
@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    // 1. We tell Mockito to create a completely fake version of our repository
    @Mock
    private UserRepository userRepository;

    // 2. We inject that fake repository into the ApplicationConfig
    @InjectMocks
    private ApplicationConfig applicationConfig;

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Arrange (Set up our mock data)
        String testEmail = "john@example.com";
        User mockUser = new User();
        // Assuming you add standard setters to your User entity:
        // mockUser.setEmail(testEmail);
        // mockUser.setPasswordHash("hashedPassword");

        // Tell the mock repository what to do when findByEmail is called
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        // Act (Run the actual method we want to test)
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();
        UserDetails result = userDetailsService.loadUserByUsername(testEmail);

        // Assert (Check the results)
        assertNotNull(result);
        // Verify that the repository was actually called exactly one time
        verify(userRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_UserDoesNotExist_ThrowsException() {
        // Arrange
        String unknownEmail = "ghost@example.com";

        // Tell the mock to return an empty Optional (simulating user not found)
        when(userRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(unknownEmail);
        });

        verify(userRepository, times(1)).findByEmail(unknownEmail);
    }
}

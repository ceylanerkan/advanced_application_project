package com.example.demo.serviceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setCustomerId("A123");
        testUser.setRoleType("INDIVIDUAL");
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));
        List<User> users = userService.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        User found = userService.getUserById(1L);
        assertEquals("A123", found.getCustomerId());
    }

    @Test
    void createUser_ShouldSaveAndReturn() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User saved = userService.createUser(testUser);
        assertNotNull(saved);
    }

    @Test
    void deleteUser_ShouldCallRepository() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}
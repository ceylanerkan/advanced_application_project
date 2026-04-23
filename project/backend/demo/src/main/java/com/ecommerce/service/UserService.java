package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers(String currentCustomerId) {
        User currentUser = userRepository.findByCustomerId(currentCustomerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all users");
        }
        return userRepository.findAll();
    }

    public User getUserByIdSecurely(Long id, String currentCustomerId) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
                
        User currentUser = userRepository.findByCustomerId(currentCustomerId).orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType()) && !targetUser.getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own profile");
        }
        return targetUser;
    }

    public User createUser(User user) {
        // Enforce default role to prevent someone from creating an ADMIN account
        if (user.getRoleType() == null || user.getRoleType().isEmpty()) {
            user.setRoleType("INDIVIDUAL");
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails, String currentCustomerId) {
        User targetUser = getUserByIdSecurely(id, currentCustomerId); // Inherits ownership check
        User currentUser = userRepository.findByCustomerId(currentCustomerId).orElseThrow();
        
        targetUser.setCustomerId(userDetails.getCustomerId());
        targetUser.setPassword(userDetails.getPassword());
        
        // Security: Mitigate AV-11 (Mass Assignment - Vertical Escalation)
        // Only admins can change a role type. 
        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            targetUser.setRoleType(userDetails.getRoleType());
        }
        
        return userRepository.save(targetUser);
    }

    public void deleteUser(Long id, String currentCustomerId) {
        User user = getUserByIdSecurely(id, currentCustomerId); // Inherits ownership check
        userRepository.delete(user);
    }
}
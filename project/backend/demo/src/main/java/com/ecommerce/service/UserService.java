package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public List<User> getAllUsers(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all users");
        }
        return userRepository.findAll();
    }

    public Page<User> getUsersPaged(String email, int page, int size) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all users");
        }
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public User getUserByIdSecurely(Long id, String email) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
                
        User currentUser = userRepository.findByEmail(email).orElseThrow();

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

    public User updateUser(Long id, User userDetails, String email) {
        User targetUser = getUserByIdSecurely(id, email); // Inherits ownership check
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        
        targetUser.setEmail(userDetails.getEmail());
        targetUser.setPasswordHash(userDetails.getPasswordHash());
        
        // Security: Mitigate AV-11 (Mass Assignment - Vertical Escalation)
        // Only admins can change a role type. 
        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            targetUser.setRoleType(userDetails.getRoleType());
        }
        
        return userRepository.save(targetUser);
    }

    public void deleteUser(Long id, String email) {
        User user = getUserByIdSecurely(id, email); // Inherits ownership check
        userRepository.delete(user);
    }
}
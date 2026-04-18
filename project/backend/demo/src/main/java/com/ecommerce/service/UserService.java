package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User createUser(User user) {
        // Note: When you fully implement Spring Security, you will need to inject a PasswordEncoder
        // here to hash the password (e.g., BCrypt) before saving it to the database.
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        user.setCustomerId(userDetails.getCustomerId());
        user.setPassword(userDetails.getPassword());
        user.setRoleType(userDetails.getRoleType());
        
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        // Fetch the user first to ensure a 404 is thrown if it does not exist
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
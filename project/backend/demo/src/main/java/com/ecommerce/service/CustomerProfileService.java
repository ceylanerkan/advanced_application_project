package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.CustomerProfile;
import com.ecommerce.model.User;
import com.ecommerce.repository.CustomerProfileRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;
    private final UserRepository userRepository;

    public List<CustomerProfile> getAllCustomerProfiles(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all customer profiles");
        }
        return customerProfileRepository.findAll();
    }

    public CustomerProfile getCustomerProfileById(Long id, String email) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerProfile not found with id: " + id));

        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Only admins or the profile's own user can view
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType()) 
                && !profile.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own profile");
        }
        return profile;
    }

    public CustomerProfile createCustomerProfile(CustomerProfile profile, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Force ownership to the logged-in user
        profile.setUser(currentUser);

        return customerProfileRepository.save(profile);
    }

    public CustomerProfile updateCustomerProfile(Long id, CustomerProfile profileDetails, String email) {
        CustomerProfile profile = getCustomerProfileById(id, email); // Inherits ownership check

        profile.setAge(profileDetails.getAge());
        profile.setCity(profileDetails.getCity());
        profile.setMembershipType(profileDetails.getMembershipType());

        return customerProfileRepository.save(profile);
    }

    public void deleteCustomerProfile(Long id, String email) {
        CustomerProfile profile = getCustomerProfileById(id, email); // Inherits ownership check
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType()) 
                && !profile.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own profile");
        }
        customerProfileRepository.delete(profile);
    }
}

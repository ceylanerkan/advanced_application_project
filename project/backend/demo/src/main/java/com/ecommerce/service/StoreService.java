package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Store;
import com.ecommerce.model.User;
import com.ecommerce.repository.StoreRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public List<Store> getAllStores(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            return storeRepository.findAll();
        } else if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) {
            // Corporate users can only see their own stores
            return storeRepository.findByOwner_Id(currentUser.getId());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individual users cannot view stores.");
    }

    public Store getStoreById(Long id, String email) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));

        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Security Check: Verify ownership
        if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) {
            if (!store.getOwner().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Store does not belong to you");
            }
        } else if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individual users cannot access store details.");
        }
        // ADMIN passes through automatically
        return store;
    }

    public Store createStore(Store store, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individual users cannot create stores.");
        }

        // Force ownership to the current user for corporate users
        if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) {
            store.setOwner(currentUser);
        }

        return storeRepository.save(store);
    }

    public Store updateStore(Long id, Store storeDetails, String email) {
        Store store = getStoreById(id, email); // Inherits ownership check
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individual users cannot update stores.");
        }

        store.setName(storeDetails.getName());
        store.setStatus(storeDetails.getStatus());

        // Only admins can reassign store ownership
        if ("ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            store.setOwner(storeDetails.getOwner());
        }

        return storeRepository.save(store);
    }

    public void deleteStore(Long id, String email) {
        Store store = getStoreById(id, email); // Inherits ownership check
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Admins can delete stores.");
        }
        storeRepository.delete(store);
    }
}

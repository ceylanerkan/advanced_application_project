package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void verifyWriteAccess(String customerId) {
        User currentUser = userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individual users cannot modify the product catalog.");
        }
    }

    public Product createProduct(Product product, String customerId) {
        verifyWriteAccess(customerId);
        
        // NOTE FOR YOUR PROJECT: To fully satisfy AV-05 (BOLA) for Corporate users, 
        // your Product model needs a `storeId` field so you can lock Corporate users to only creating/editing 
        // products for their specific store. Once added, you would inject it here:
        // if ("CORPORATE".equalsIgnoreCase(currentUser.getRoleType())) { product.setStoreId(currentUser.getStoreId()); }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails, String customerId) {
        verifyWriteAccess(customerId);
        Product product = getProductById(id);
        
        product.setProductId(productDetails.getProductId());
        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id, String customerId) {
        verifyWriteAccess(customerId);
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
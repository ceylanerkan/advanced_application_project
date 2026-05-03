package com.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Page<Product> getProductsPaged(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void verifyWriteAccess(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if ("INDIVIDUAL".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Individual users cannot modify the product catalog.");
        }
    }

    public Product createProduct(Product product, String email) {
        verifyWriteAccess(email);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails, String email) {
        verifyWriteAccess(email);
        Product product = getProductById(id);
        
        product.setStore(productDetails.getStore());
        product.setCategory(productDetails.getCategory());
        product.setSku(productDetails.getSku());
        product.setName(productDetails.getName());
        product.setUnitPrice(productDetails.getUnitPrice());
        product.setBaseCurrency(productDetails.getBaseCurrency());
        product.setOriginalCurrency(productDetails.getOriginalCurrency());
        product.setExchangeRate(productDetails.getExchangeRate());
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id, String email) {
        verifyWriteAccess(email);
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
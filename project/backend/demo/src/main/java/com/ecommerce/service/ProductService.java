package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        
        product.setProductId(productDetails.getProductId());
        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        // Fetch the product first so it throws a 404 if it doesn't exist
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
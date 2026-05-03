package com.ecommerce.controller;

import java.util.List;

import org.springframework.data.domain.Page;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing the store catalog")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products", description = "Retrieves products. Add ?page=0&size=20 for paginated results.")
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Page<Product> result = productService.getProductsPaged(page, size);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Get a product by ID", description = "Retrieves the details of a specific product.")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Create a new product", description = "Adds a new product. Restricted to ADMIN and CORPORATE users.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individuals cannot create products")
    })
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product, Authentication authentication) {
        return new ResponseEntity<>(productService.createProduct(product, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing product", description = "Updates a product. Restricted to ADMIN and CORPORATE users.")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails, Authentication authentication) {
        return ResponseEntity.ok(productService.updateProduct(id, productDetails, authentication.getName()));
    }

    @Operation(summary = "Delete a product", description = "Deletes a specific product. Restricted to ADMIN and CORPORATE users.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication) {
        productService.deleteProduct(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
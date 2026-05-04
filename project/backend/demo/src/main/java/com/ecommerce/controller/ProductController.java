package com.ecommerce.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.data.domain.Page;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

    @Operation(summary = "Get my products", description = "Returns only products belonging to the current user's stores.")
    @GetMapping("/mine")
    public ResponseEntity<List<Product>> getMyProducts(Authentication authentication) {
        return ResponseEntity.ok(productService.getMyProducts(authentication.getName()));
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

    @Operation(
        summary = "Populate product images (Admin only)",
        description = "One-time utility: assigns a stable Unsplash Source image URL to every product that currently has no image. Safe to call multiple times — skips products that already have a URL."
    )
    @PostMapping("/populate-images")
    public ResponseEntity<Map<String, Object>> populateImages(
            Authentication authentication,
            @RequestParam(defaultValue = "100") int limit) {
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType())) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.FORBIDDEN, "Only admins can populate images.");
        }

        List<Product> products = productRepository.findAll();
        int updated = 0;

        for (Product p : products) {
            if (updated >= limit) break;           // stop after limit
            if (p.getImageUrl() == null || p.getImageUrl().isBlank()) {
            // Picsum Photos: stable image per product, no API key needed
                p.setImageUrl("https://picsum.photos/seed/" + p.getId() + "/400/300");
                productRepository.save(p);
                updated++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalProducts", products.size());
        result.put("imagesAssigned", updated);
        result.put("limit", limit);
        result.put("message", updated + " products received image URLs (limit=" + limit + "). "
            + (products.size() - updated) + " skipped.");
        return ResponseEntity.ok(result);
    }
}
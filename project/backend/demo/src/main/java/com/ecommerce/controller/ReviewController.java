package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.model.Review;
import com.ecommerce.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "Endpoints for managing customer product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get all reviews", description = "Retrieves a complete list of all reviews in the system.")
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @Operation(summary = "Get a review by ID", description = "Retrieves the details of a specific review.")
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @Operation(summary = "Create a new review", description = "Creates a new product review. Validates required fields before saving.")
    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        return new ResponseEntity<>(reviewService.createReview(review), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing review", description = "Updates a review by its ID. Validates required fields.")
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @Valid @RequestBody Review reviewDetails) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewDetails));
    }

    @Operation(summary = "Delete a review", description = "Deletes a specific review by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
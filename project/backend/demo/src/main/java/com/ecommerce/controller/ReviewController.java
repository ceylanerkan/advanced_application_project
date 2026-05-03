package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Get all reviews", description = "Publicly accessible list of reviews.")
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @Operation(summary = "Get reviews for my store", description = "Returns reviews for products belonging to the authenticated user's stores. Admins see all.")
    @GetMapping("/my-store")
    public ResponseEntity<List<Review>> getMyStoreReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getReviewsForMyStore(authentication.getName()));
    }

    @Operation(summary = "Get a review by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @Operation(summary = "Create a new review", description = "Creates a review securely. Mitigates AV-04 (Stored XSS) and AV-11 (Mass Assignment).")
    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review, Authentication authentication) {
        return new ResponseEntity<>(reviewService.createReview(review, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a review", description = "Updates your own review. XSS sanitized.")
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @Valid @RequestBody Review reviewDetails, Authentication authentication) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewDetails, authentication.getName()));
    }

    @Operation(summary = "Delete a review", description = "Delete your own review or any if Admin.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Authentication authentication) {
        reviewService.deleteReview(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
package com.ecommerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    public Review createReview(Review review, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        review.setUser(currentUser);

        if (review.getProduct() == null || review.getProduct().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
        }
        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        review.setProduct(product);

        review.setCreatedAt(LocalDateTime.now().toString());

        return reviewRepository.save(review);
    }

    public Review updateReview(Long id, Review reviewDetails, String email) {
        Review review = getReviewById(id);
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType()) && !review.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own reviews");
        }

        review.setStarRating(reviewDetails.getStarRating());
        review.setComment(reviewDetails.getComment());
        review.setSentiment(reviewDetails.getSentiment());
        review.setProduct(reviewDetails.getProduct());
        // createdAt is intentionally not updated — preserve original timestamp
        
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id, String email) {
        Review review = getReviewById(id);
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRoleType()) && !review.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reviews");
        }
        reviewRepository.delete(review);
    }
}
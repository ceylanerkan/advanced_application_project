package com.ecommerce.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    public Review createReview(Review review, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        
        // Mitigate AV-11: Force review ownership to logged in user
        review.setUser(currentUser);

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
        review.setCreatedAt(reviewDetails.getCreatedAt());
        
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
package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.model.Review;
import com.ecommerce.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public Review updateReview(Long id, Review reviewDetails) {
        Review review = getReviewById(id);
        review.setReviewId(reviewDetails.getReviewId());
        review.setUser(reviewDetails.getUser());
        review.setProduct(reviewDetails.getProduct());
        review.setStarRating(reviewDetails.getStarRating());
        review.setHelpfulVotes(reviewDetails.getHelpfulVotes());
        review.setTotalVotes(reviewDetails.getTotalVotes());
        review.setSentiment(reviewDetails.getSentiment());
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
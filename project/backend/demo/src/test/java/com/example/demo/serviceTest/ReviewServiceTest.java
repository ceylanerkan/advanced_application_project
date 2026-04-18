package com.example.demo.serviceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.model.Review;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.service.ReviewService;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;

    @BeforeEach
    void setUp() {
        testReview = new Review();
        testReview.setId(1L);
        testReview.setReviewId("R123");
        testReview.setStarRating(5);
    }

    @Test
    void getAllReviews_ShouldReturnList() {
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(testReview));
        List<Review> reviews = reviewService.getAllReviews();
        assertEquals(1, reviews.size());
    }

    @Test
    void getReviewById_ShouldReturnReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        Review found = reviewService.getReviewById(1L);
        assertEquals(5, found.getStarRating());
    }
}
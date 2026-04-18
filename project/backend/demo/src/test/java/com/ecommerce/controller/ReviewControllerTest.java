package com.ecommerce.controller;

import com.ecommerce.model.Review;
import com.ecommerce.security.service.JwtService;
import com.ecommerce.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;
    
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ReviewService reviewService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Review testReview;

    @BeforeEach
    void setUp() {
        testReview = new Review();
        testReview.setId(1L);
        testReview.setReviewId("R123");
        testReview.setStarRating(5);
    }

    @Test
    void getAllReviews_ShouldReturn200() throws Exception {
        Mockito.when(reviewService.getAllReviews()).thenReturn(List.of(testReview));
        
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].starRating").value(5));
    }
}
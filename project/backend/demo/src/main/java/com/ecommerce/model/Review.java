package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Review ID cannot be blank")
    @Column(unique = true, nullable = false)
    private String reviewId; // Amazon'daki yorum ID'si

    // Foreign Key: Yorumu yapan kullanıcı
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Foreign Key: Yorum yapılan ürün
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating cannot exceed 5")
    private Integer starRating;

    // ADDED THIS: The actual text of the review (needed for XSS testing)
    @NotBlank(message = "Review comment cannot be empty")
    @Column(columnDefinition = "TEXT")
    private String comment; 

    private Integer helpfulVotes = 0; // Good practice to default to 0
    private Integer totalVotes = 0;

    @Column(length = 50)
    private String sentiment; // Al'ın dolduracağı duygu durumu
}
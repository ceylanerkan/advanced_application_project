package com.ecommerce.model;

import jakarta.persistence.*;
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

    private Integer starRating;
    private Integer helpfulVotes;
    private Integer totalVotes;

    @Column(length = 50)
    private String sentiment; // Al'ın dolduracağı duygu durumu

    
}

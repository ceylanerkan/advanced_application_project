package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "base_currency")
    private String baseCurrency;

    @Column(name = "original_currency")
    private String originalCurrency;

    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
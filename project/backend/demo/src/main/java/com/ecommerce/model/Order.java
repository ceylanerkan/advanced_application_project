package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private String status;

    @Column(name = "grand_total")
    private Double grandTotal;

    @Column(name = "base_currency")
    private String baseCurrency;

    @Column(name = "original_currency")
    private String originalCurrency;

    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @Column(name = "shipping_full_name")
    private String shippingFullName;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_city")
    private String shippingCity;

    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;

    @Column(name = "shipping_phone")
    private String shippingPhone;

    @Column(name = "created_at")
    private String createdAt; // ISO 8601 String
}
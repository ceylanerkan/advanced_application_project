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

    @Column(unique = true, nullable = false)
    private String productId; // Amazon'daki ürün ID'si (Örn: B0000...)

    @Column(nullable = false)
    private String name; // Amazon'daki ProductTitle

    // Foreign Key: Bir ürünün bir kategorisi olur
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    
}
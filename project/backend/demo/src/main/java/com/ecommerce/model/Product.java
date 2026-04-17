package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Product ID (Amazon ID) cannot be blank")
    @Column(unique = true, nullable = false)
    private String productId; // Amazon'daki ürün ID'si (Örn: B0000...)

    @NotBlank(message = "Product name cannot be empty")
    @Column(nullable = false)
    private String name; // Amazon'daki ProductTitle

    // Foreign Key: Bir ürünün bir kategorisi olur
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
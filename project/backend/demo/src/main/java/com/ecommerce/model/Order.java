package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // Siparişi hangi kullanıcı verdi? (Foreign Key)
    @NotNull(message = "User must be specified for the order")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Order status is mandatory")
    private String status; // Örn: Completed, Canceled, Pending
    
    @NotNull(message = "Grand total cannot be null")
    @Min(value = 0, message = "Grand total cannot be negative")
    private Double grandTotal; // Toplam tutar
    
    // İleride kurumsal mağazalar eklenirse diye tutuyoruz
    @NotNull(message = "Store ID must be specified")
    private Long storeId; 
}
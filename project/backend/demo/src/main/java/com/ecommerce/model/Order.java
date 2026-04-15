package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String status; // Örn: Completed, Canceled, Pending
    private Double grandTotal; // Toplam tutar
    
    // İleride kurumsal mağazalar eklenirse diye tutuyoruz
    private Long storeId; 

    // TODO: Sağ tıklayıp Getter ve Setter'ları oluştur
}
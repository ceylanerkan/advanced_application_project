package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi siparişin kargosu? (Foreign Key)
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @NotBlank(message = "Warehouse block must be specified")
    private String warehouse; // Örn: Block A, Block B
    
    @NotBlank(message = "Shipment mode must be specified")
    private String mode; // Örn: Flight, Ship, Road
    
    @NotBlank(message = "Shipment status is mandatory")
    private String status; // Örn: Delivered, In Transit
}
package com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecommerce.model.Order;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByUserId(Long userId);
    // Finds all orders linked to a user with this specific email
    List<Order> findByUser_Email(String email);
    
}
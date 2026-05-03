package com.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ecommerce.model.Order;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByUserId(Long userId);
    List<Order> findByUser_Email(String email);
    Page<Order> findByUser_Email(String email, Pageable pageable);

    List<Order> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.user.id = :userId")
    Double sumGrandTotalByUserId(Long userId);

    List<Order> findByStoreId(Long storeId);
    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    long countByStoreId(Long storeId);
}
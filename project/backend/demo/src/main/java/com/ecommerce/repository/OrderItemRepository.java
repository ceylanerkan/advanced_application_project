package com.ecommerce.repository;

import com.ecommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

    List<OrderItem> findByOrder_UserId(Long userId);

    long countByOrder_Id(Long orderId);

    @Query("SELECT COALESCE(SUM(oi.price * oi.quantity), 0) * 1.08 FROM OrderItem oi")
    Double sumTotalRevenue();
}

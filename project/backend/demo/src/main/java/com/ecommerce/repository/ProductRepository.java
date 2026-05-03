package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    long countByStoreId(Long storeId);
    List<Product> findByStoreId(Long storeId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(p.averageRating) FROM Product p WHERE p.store.id = :storeId AND p.averageRating IS NOT NULL")
    Double getAverageRatingByStoreId(Long storeId);

    long countByCategoryId(Long categoryId);
}

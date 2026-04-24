package com.ecommerce.repository;

import com.ecommerce.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByOwner_Id(Long ownerId);
}

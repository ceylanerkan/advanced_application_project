package com.ecommerce.repository;

import com.ecommerce.model.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByUser_Id(Long userId);

    Optional<CustomerProfile> findByUser_Email(String email);
}

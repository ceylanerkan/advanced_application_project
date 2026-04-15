package com.example.demo; // Kendi paket adına göre teyit et (örn: com.ecommerce.demo.repository olabilir)

import org.springframework.data.jpa.repository.JpaRepository; // Kendi yazdığımız Category modelini çağırıyoruz

import com.ecommerce.model.Category;

// Bu bir "class" değil, "interface"dir. JpaRepository'den miras alır.
public interface CategoryRepository extends JpaRepository<Category, Long> {

} 
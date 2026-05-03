package com.ecommerce.controller; // Kendi paket adını kontrol et

import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/users")
public class DashboardController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    // @GetMapping("/{userId}/dashboard")
    // public ResponseEntity<Map<String, Object>>
    // getIndividualDashboard(@PathVariable Long userId) {

    // Map<String, Object> response = new HashMap<>();

    // // GERÇEK KPI VERİLERİ (Veri tabanından anlık olarak sayılır)
    // long totalReviews = reviewRepository.countByUserId(userId);
    // long totalOrders = orderRepository.countByUserId(userId);

    // response.put("totalReviews", totalReviews);
    // response.put("totalOrders", totalOrders);
    // response.put("totalSpent", 0); // Bunu daha sonra SUM(grand_total) ile
    // doldurabilirsiniz

    // // GRAFİK VERİLERİ (Şimdilik grafikleri boş/statik bırakıyoruz)
    // response.put("spendLabels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May",
    // "Jun"));
    // response.put("spendValues", Arrays.asList(0, 0, 0, 0, 0, 0));

    // response.put("categoryLabels", Arrays.asList("Electronics", "Books",
    // "Clothing"));
    // response.put("categoryValues", Arrays.asList(0, 0, 0));

    // return ResponseEntity.ok(response);
    // }

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<Map<String, Object>> getIndividualDashboard(@PathVariable Long userId) {

        System.out.println("====> İSTEK GELDİ! Arayan User ID: " + userId);

        Map<String, Object> response = new HashMap<>();

        long totalReviews = reviewRepository.countByUserId(userId);
        long totalOrders = orderRepository.countByUserId(userId);

        System.out.println("====> VERİ TABANI CEVABI: Yorum=" + totalReviews + " | Sipariş=" + totalOrders);

        response.put("totalReviews", totalReviews);
        response.put("totalOrders", totalOrders);
        response.put("totalSpent", 0);

        response.put("spendLabels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun"));
        response.put("spendValues", Arrays.asList(0, 0, 0, 0, 0, 0));
        response.put("categoryLabels", Arrays.asList("Electronics", "Books", "Clothing"));
        response.put("categoryValues", Arrays.asList(0, 0, 0));

        return ResponseEntity.ok(response);
    }
}

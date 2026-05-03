package com.ecommerce.controller;

import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/users")
public class DashboardController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<Map<String, Object>> getIndividualDashboard(@PathVariable Long userId) {

        System.out.println("====> İSTEK GELDİ! Arayan User ID: " + userId);

        Map<String, Object> response = new HashMap<>();

        long totalReviews = reviewRepository.countByUserId(userId);
        long totalOrders = orderRepository.countByUserId(userId);
        Double totalSpentDb = orderRepository.sumGrandTotalByUserId(userId);
        double totalSpent = totalSpentDb != null ? totalSpentDb : 0.0;

        // Calculate Spend Values (Last 6 Months)
        List<Order> orders = orderRepository.findByUserId(userId);
        double[] monthlySpend = new double[6];
        String[] monthLabels = new String[6];
        
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 6; i++) {
            LocalDate monthDate = now.minusMonths(5 - i);
            monthLabels[i] = monthDate.format(DateTimeFormatter.ofPattern("MMM"));
            
            final int currentMonth = monthDate.getMonthValue();
            final int currentYear = monthDate.getYear();
            
            double sum = orders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    try {
                        LocalDate orderDate = LocalDate.parse(o.getCreatedAt().substring(0, 10));
                        return orderDate.getMonthValue() == currentMonth && orderDate.getYear() == currentYear;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .mapToDouble(o -> o.getGrandTotal() != null ? o.getGrandTotal() : 0.0)
                .sum();
            monthlySpend[i] = sum;
        }

        // Calculate Category Values
        List<OrderItem> items = orderItemRepository.findByOrder_UserId(userId);
        Map<String, Double> categoryTotals = new HashMap<>();
        for (OrderItem item : items) {
            String catName = (item.getProduct() != null && item.getProduct().getCategory() != null) 
                ? item.getProduct().getCategory().getName() 
                : "Uncategorized";
            
            double lineTotal = (item.getPrice() != null ? item.getPrice() : 0) * (item.getQuantity() != null ? item.getQuantity() : 0);
            categoryTotals.put(catName, categoryTotals.getOrDefault(catName, 0.0) + lineTotal);
        }

        List<String> catLabels = new ArrayList<>(categoryTotals.keySet());
        List<Double> catValues = new ArrayList<>(categoryTotals.values());
        
        // If empty, provide defaults so chart doesn't break
        if (catLabels.isEmpty()) {
            catLabels = Arrays.asList("Electronics", "Books", "Clothing");
            catValues = Arrays.asList(0.0, 0.0, 0.0);
        }

        List<Double> spendValuesList = new ArrayList<>();
        for(double d : monthlySpend) spendValuesList.add(d);

        response.put("totalReviews", totalReviews);
        response.put("totalOrders", totalOrders);
        response.put("totalSpent", totalSpent);
        response.put("spendLabels", Arrays.asList(monthLabels));
        response.put("spendValues", spendValuesList);
        response.put("categoryLabels", catLabels);
        response.put("categoryValues", catValues);

        return ResponseEntity.ok(response);
    }
}

package com.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.Store;
import com.ecommerce.service.StoreService;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store Management", description = "Endpoints for managing stores (Corporate and Admin)")
public class StoreController {

    private final StoreService storeService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @GetMapping("/{storeId}/dashboard")
    public ResponseEntity<Map<String, Object>> getStoreDashboard(@PathVariable Long storeId, Authentication authentication) {
        // Ensure user has access (simplified for demo)
        storeService.getStoreById(storeId, authentication.getName());
        
        Map<String, Object> response = new HashMap<>();

        // KPI
        List<Order> orders = orderRepository.findByStoreId(storeId);
        long totalOrders = orders.size();
        double totalRevenue = orders.stream().mapToDouble(o -> o.getGrandTotal() != null ? o.getGrandTotal() : 0.0).sum();
        long activeProducts = productRepository.countByStoreId(storeId);
        Double avgRatingDb = productRepository.getAverageRatingByStoreId(storeId);
        double avgRating = avgRatingDb != null ? avgRatingDb : 0.0;

        // Weekly Orders
        double[] weeklyOrders = new double[7];
        String[] weekLabels = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        // Simplified: Just assigning some real order count to random days or real days if dates are proper
        for (Order o : orders) {
            if (o.getCreatedAt() != null) {
                try {
                    LocalDate d = LocalDate.parse(o.getCreatedAt().substring(0, 10));
                    int day = d.getDayOfWeek().getValue() - 1; // 0=Mon, 6=Sun
                    weeklyOrders[day]++;
                } catch(Exception ignored){}
            }
        }
        
        List<Double> weekValuesList = new ArrayList<>();
        for(double d : weeklyOrders) weekValuesList.add(d);

        // Monthly Revenue and Orders
        double[] monthlyRevenue = new double[6];
        double[] monthlyOrders = new double[6];
        String[] monthLabels = new String[6];
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 6; i++) {
            LocalDate monthDate = now.minusMonths(5 - i);
            monthLabels[i] = monthDate.format(DateTimeFormatter.ofPattern("MMM"));
            final int m = monthDate.getMonthValue();
            final int y = monthDate.getYear();
            monthlyRevenue[i] = orders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    try {
                        LocalDate d = LocalDate.parse(o.getCreatedAt().substring(0, 10));
                        return d.getMonthValue() == m && d.getYear() == y;
                    } catch(Exception e) { return false; }
                })
                .mapToDouble(o -> o.getGrandTotal() != null ? o.getGrandTotal() : 0.0)
                .sum();
                
            monthlyOrders[i] = orders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    try {
                        LocalDate d = LocalDate.parse(o.getCreatedAt().substring(0, 10));
                        return d.getMonthValue() == m && d.getYear() == y;
                    } catch(Exception e) { return false; }
                })
                .count();
        }
        List<Double> monthlyRevList = new ArrayList<>();
        List<Double> monthlyOrdersList = new ArrayList<>();
        for(double d : monthlyRevenue) monthlyRevList.add(d);
        for(double d : monthlyOrders) monthlyOrdersList.add(d);

        // Sales by Category
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Order o : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder_Id(o.getId());
            for (OrderItem item : items) {
                String catName = (item.getProduct() != null && item.getProduct().getCategory() != null) 
                    ? item.getProduct().getCategory().getName() 
                    : "Uncategorized";
                double lineTotal = (item.getPrice() != null ? item.getPrice() : 0) * (item.getQuantity() != null ? item.getQuantity() : 0);
                categoryTotals.put(catName, categoryTotals.getOrDefault(catName, 0.0) + lineTotal);
            }
        }
        List<String> catLabels = new ArrayList<>(categoryTotals.keySet());
        List<Double> catValues = new ArrayList<>(categoryTotals.values());
        if (catLabels.isEmpty()) {
            catLabels = Arrays.asList("Electronics", "Books", "Clothing");
            catValues = Arrays.asList(0.0, 0.0, 0.0);
        }

        // Customer Insights (City and Customers)
        Map<String, Integer> cityTotals = new HashMap<>();
        Map<String, Map<String, Object>> customerStats = new HashMap<>();

        for (Order o : orders) {
            String city = o.getShippingCity() != null ? o.getShippingCity() : "Unknown";
            cityTotals.put(city, cityTotals.getOrDefault(city, 0) + 1);

            String email = o.getUser() != null ? o.getUser().getEmail() : "Unknown";
            customerStats.putIfAbsent(email, new HashMap<>());
            Map<String, Object> stats = customerStats.get(email);
            stats.put("email", email);
            stats.put("city", city);
            stats.put("orders", (int)stats.getOrDefault("orders", 0) + 1);
            stats.put("totalSpent", (double)stats.getOrDefault("totalSpent", 0.0) + (o.getGrandTotal() != null ? o.getGrandTotal() : 0.0));
        }

        List<String> cityLabels = new ArrayList<>(cityTotals.keySet());
        List<Integer> cityValues = new ArrayList<>(cityTotals.values());
        if (cityLabels.isEmpty()) {
            cityLabels = Arrays.asList("New York", "Los Angeles", "Chicago", "Houston", "Phoenix");
            cityValues = Arrays.asList(0, 0, 0, 0, 0);
        }

        List<Map<String, Object>> customersList = new ArrayList<>(customerStats.values());
        for (Map<String, Object> c : customersList) {
            double spent = (double) c.get("totalSpent");
            c.put("membership", spent > 2000 ? "Gold" : (spent > 1000 ? "Silver" : "Bronze"));
        }

        long goldCount = customersList.stream().filter(c -> "Gold".equals(c.get("membership"))).count();
        long silverCount = customersList.stream().filter(c -> "Silver".equals(c.get("membership"))).count();
        long bronzeCount = customersList.stream().filter(c -> "Bronze".equals(c.get("membership"))).count();
        
        List<Long> membershipValues = Arrays.asList(goldCount, silverCount, bronzeCount);
        if (customersList.isEmpty()) {
            membershipValues = Arrays.asList(0L, 0L, 0L);
        }

        response.put("totalRevenue", totalRevenue);
        response.put("totalOrders", totalOrders);
        response.put("activeProducts", activeProducts);
        response.put("avgRating", String.format("%.1f", avgRating).replace(",", "."));

        response.put("weekLabels", Arrays.asList(weekLabels));
        response.put("weekValues", weekValuesList);

        response.put("monthLabels", Arrays.asList(monthLabels));
        response.put("monthValues", monthlyRevList);
        response.put("monthOrdersValues", monthlyOrdersList);

        response.put("categoryLabels", catLabels);
        response.put("categoryValues", catValues);

        response.put("cityLabels", cityLabels);
        response.put("cityValues", cityValues);
        response.put("membershipLabels", Arrays.asList("Gold", "Silver", "Bronze"));
        response.put("membershipValues", membershipValues);
        response.put("topCustomers", customersList);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all stores", description = "Admins see all stores; Corporate users see only their own. Individual users are denied.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stores retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individual users cannot view stores")
    })
    @GetMapping
    public ResponseEntity<List<Store>> getAllStores(Authentication authentication) {
        return ResponseEntity.ok(storeService.getAllStores(authentication.getName()));
    }

    @Operation(summary = "Get a store by ID", description = "Retrieves a specific store. Corporate users can only view their own stores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Store does not belong to you"),
            @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(storeService.getStoreById(id, authentication.getName()));
    }

    @Operation(summary = "Create a new store", description = "Creates a new store. Corporate users are automatically set as the owner. Individuals are denied.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Store created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Individual users cannot create stores")
    })
    @PostMapping
    public ResponseEntity<Store> createStore(@Valid @RequestBody Store store, Authentication authentication) {
        return new ResponseEntity<>(storeService.createStore(store, authentication.getName()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a store", description = "Updates store details. Only Admins can reassign ownership.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Unauthorized to update this store")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Long id, @Valid @RequestBody Store storeDetails, Authentication authentication) {
        return ResponseEntity.ok(storeService.updateStore(id, storeDetails, authentication.getName()));
    }

    @Operation(summary = "Delete a store (Admin only)", description = "Deletes a store. Only Admins can perform this action.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only Admins can delete stores")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id, Authentication authentication) {
        storeService.deleteStore(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

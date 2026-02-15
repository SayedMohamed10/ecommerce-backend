package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    private UserStats userStats;
    private List<RecentActivity> recentActivities;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Integer totalOrders;
        private BigDecimal totalSpent;
        private Integer pendingOrders;
        private Integer completedOrders;
        private Integer savedItems; // Wishlist count
        private Integer cartItems;
        private LocalDateTime memberSince;
        private Boolean emailVerified;
        private Boolean twoFactorEnabled;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String type; // ORDER_PLACED, ORDER_DELIVERED, REVIEW_ADDED, etc.
        private String description;
        private LocalDateTime timestamp;
        private String icon;
        private String color;
    }
}

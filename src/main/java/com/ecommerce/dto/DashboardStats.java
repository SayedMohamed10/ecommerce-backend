package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    
    // Overview Stats
    private Long totalOrders;
    private Long totalUsers;
    private Long totalProducts;
    private Long totalReviews;
    
    // Revenue Stats
    private BigDecimal totalRevenue;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisWeek;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueThisYear;
    
    // Order Stats
    private Long ordersToday;
    private Long ordersThisWeek;
    private Long ordersThisMonth;
    private Long ordersPending;
    private Long ordersProcessing;
    private Long ordersShipped;
    private Long ordersDelivered;
    private Long ordersCancelled;
    
    // User Stats
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Long activeUsers; // Users who placed orders
    
    // Product Stats
    private Long activeProducts;
    private Long inactiveProducts;
    private Long lowStockProducts; // Stock < 10
    private Long outOfStockProducts; // Stock = 0
    
    // Review Stats
    private Long pendingReviews;
    private Double averageRating;
    
    // Payment Stats
    private Long successfulPayments;
    private Long failedPayments;
    private BigDecimal pendingPaymentAmount;
    
    // Top Performing
    private List<TopSellingProduct> topSellingProducts;
    private List<TopRevenueProduct> topRevenueProducts;
    private List<RecentOrder> recentOrders;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingProduct {
        private Long productId;
        private String productName;
        private String productImage;
        private Long totalSold;
        private BigDecimal revenue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRevenueProduct {
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal revenue;
        private Long unitsSold;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long orderId;
        private String orderNumber;
        private String customerName;
        private String customerEmail;
        private BigDecimal totalAmount;
        private String status;
        private String createdAt;
    }
}

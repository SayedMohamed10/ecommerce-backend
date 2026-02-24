package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalytics {
    
    // Period Summary
    private String period; // TODAY, WEEK, MONTH, YEAR, CUSTOM
    private String startDate;
    private String endDate;
    
    // Sales Summary
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Long totalItemsSold;
    
    // Comparison (vs previous period)
    private BigDecimal revenueGrowth; // Percentage
    private Long orderGrowth; // Percentage
    
    // Daily/Hourly Breakdown
    private List<SalesDataPoint> salesByDay;
    private List<SalesDataPoint> salesByHour;
    
    // Category Breakdown
    private List<CategorySales> salesByCategory;
    
    // Payment Method Breakdown
    private Map<String, BigDecimal> salesByPaymentMethod;
    
    // Order Status Distribution
    private Map<String, Long> ordersByStatus;
    
    // Top Customers
    private List<TopCustomer> topCustomers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesDataPoint {
        private String label; // Date or hour
        private BigDecimal revenue;
        private Long orders;
        private Long items;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySales {
        private Long categoryId;
        private String categoryName;
        private BigDecimal revenue;
        private Long orders;
        private Long itemsSold;
        private Double percentage; // % of total revenue
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long userId;
        private String customerName;
        private String customerEmail;
        private Long totalOrders;
        private BigDecimal totalSpent;
        private String lastOrderDate;
    }
}

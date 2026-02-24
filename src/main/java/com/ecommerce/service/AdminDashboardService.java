package com.ecommerce.service;

import com.ecommerce.dto.DashboardStats;
import com.ecommerce.dto.SalesAnalytics;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    // Get dashboard statistics
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);
        LocalDateTime yearStart = now.minusYears(1);
        
        // Overview Stats
        stats.setTotalOrders(orderRepository.count());
        stats.setTotalUsers(userRepository.count());
        stats.setTotalProducts(productRepository.count());
        stats.setTotalReviews(reviewRepository.count());
        
        // Revenue Stats
        stats.setTotalRevenue(getTotalRevenue());
        stats.setRevenueToday(getRevenueByPeriod(todayStart, now));
        stats.setRevenueThisWeek(getRevenueByPeriod(weekStart, now));
        stats.setRevenueThisMonth(getRevenueByPeriod(monthStart, now));
        stats.setRevenueThisYear(getRevenueByPeriod(yearStart, now));
        
        // Order Stats
        stats.setOrdersToday(countOrdersByPeriod(todayStart, now));
        stats.setOrdersThisWeek(countOrdersByPeriod(weekStart, now));
        stats.setOrdersThisMonth(countOrdersByPeriod(monthStart, now));
        stats.setOrdersPending(orderRepository.countByStatus(Order.OrderStatus.PENDING));
        stats.setOrdersProcessing(orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        stats.setOrdersShipped(orderRepository.countByStatus(Order.OrderStatus.SHIPPED));
        stats.setOrdersDelivered(orderRepository.countByStatus(Order.OrderStatus.DELIVERED));
        stats.setOrdersCancelled(orderRepository.countByStatus(Order.OrderStatus.CANCELLED));
        
        // User Stats
        stats.setNewUsersToday(countNewUsersByPeriod(todayStart, now));
        stats.setNewUsersThisWeek(countNewUsersByPeriod(weekStart, now));
        stats.setNewUsersThisMonth(countNewUsersByPeriod(monthStart, now));
        stats.setActiveUsers(countActiveUsers());
        
        // Product Stats
        stats.setActiveProducts(productRepository.countByActiveTrue());
        stats.setInactiveProducts(productRepository.countByActiveFalse());
        stats.setLowStockProducts(productRepository.countByStockLessThan(10));
        stats.setOutOfStockProducts(productRepository.countByStock(0));
        
        // Review Stats
        stats.setPendingReviews(reviewRepository.countByStatus(Review.ReviewStatus.PENDING));
        stats.setAverageRating(getAverageRating());
        
        // Payment Stats
        stats.setSuccessfulPayments(paymentRepository.countByStatus(com.ecommerce.model.Payment.PaymentStatus.SUCCEEDED));
        stats.setFailedPayments(paymentRepository.countByStatus(com.ecommerce.model.Payment.PaymentStatus.FAILED));
        stats.setPendingPaymentAmount(getPendingPaymentAmount());
        
        // Top Performing
        stats.setTopSellingProducts(getTopSellingProducts(5));
        stats.setTopRevenueProducts(getTopRevenueProducts(5));
        stats.setRecentOrders(getRecentOrders(10));
        
        return stats;
    }
    
    // Get sales analytics
    public SalesAnalytics getSalesAnalytics(String period, LocalDate startDate, LocalDate endDate) {
        SalesAnalytics analytics = new SalesAnalytics();
        
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();
        
        // Determine period
        switch (period.toUpperCase()) {
            case "TODAY":
                start = LocalDate.now().atStartOfDay();
                analytics.setPeriod("TODAY");
                break;
            case "WEEK":
                start = end.minusWeeks(1);
                analytics.setPeriod("WEEK");
                break;
            case "MONTH":
                start = end.minusMonths(1);
                analytics.setPeriod("MONTH");
                break;
            case "YEAR":
                start = end.minusYears(1);
                analytics.setPeriod("YEAR");
                break;
            case "CUSTOM":
                start = startDate.atStartOfDay();
                end = endDate.atTime(23, 59, 59);
                analytics.setPeriod("CUSTOM");
                break;
            default:
                start = end.minusMonths(1);
                analytics.setPeriod("MONTH");
        }
        
        analytics.setStartDate(start.format(DateTimeFormatter.ISO_LOCAL_DATE));
        analytics.setEndDate(end.format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // Get orders in period
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        
        // Sales Summary
        analytics.setTotalOrders((long) orders.size());
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.setTotalRevenue(totalRevenue);
        
        if (orders.size() > 0) {
            analytics.setAverageOrderValue(
                totalRevenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP)
            );
        } else {
            analytics.setAverageOrderValue(BigDecimal.ZERO);
        }
        
        Long totalItems = orders.stream()
                .mapToLong(o -> o.getOrderItems().size())
                .sum();
        analytics.setTotalItemsSold(totalItems);
        
        // Growth comparison (simplified - comparing to previous period)
        LocalDateTime prevStart = start.minusDays(end.toLocalDate().toEpochDay() - start.toLocalDate().toEpochDay());
        List<Order> prevOrders = orderRepository.findByCreatedAtBetween(prevStart, start);
        
        if (!prevOrders.isEmpty()) {
            BigDecimal prevRevenue = prevOrders.stream()
                    .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growth = totalRevenue.subtract(prevRevenue)
                        .divide(prevRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                analytics.setRevenueGrowth(growth);
            }
            
            long orderGrowth = ((orders.size() - prevOrders.size()) * 100L) / prevOrders.size();
            analytics.setOrderGrowth(orderGrowth);
        }
        
        // Sales by day
        analytics.setSalesByDay(getSalesByDay(orders));
        
        // Sales by category
        analytics.setSalesByCategory(getSalesByCategory(orders));
        
        // Sales by payment method
        analytics.setSalesByPaymentMethod(getSalesByPaymentMethod(orders));
        
        // Orders by status
        analytics.setOrdersByStatus(getOrdersByStatus(orders));
        
        // Top customers
        analytics.setTopCustomers(getTopCustomers(orders, 10));
        
        return analytics;
    }
    
    // Helper methods
    
    private BigDecimal getTotalRevenue() {
        try {
            BigDecimal revenue = orderRepository.getTotalSales();
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    private BigDecimal getRevenueByPeriod(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        return orders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Long countOrdersByPeriod(LocalDateTime start, LocalDateTime end) {
        return (long) orderRepository.findByCreatedAtBetween(start, end).size();
    }
    
    private Long countNewUsersByPeriod(LocalDateTime start, LocalDateTime end) {
        return userRepository.countByCreatedAtBetween(start, end);
    }
    
    private Long countActiveUsers() {
        // Users who have placed at least one order
        return userRepository.count(); // Simplified
    }
    
    private Double getAverageRating() {
        try {
            Double avg = reviewRepository.getAverageRatingByProductId(null);
            return avg != null ? avg : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private BigDecimal getPendingPaymentAmount() {
        // Simplified - calculate from pending orders
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING, 
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        return pendingOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<DashboardStats.TopSellingProduct> getTopSellingProducts(int limit) {
        List<Object[]> results = orderItemRepository.findBestSellingProducts();
        
        return results.stream()
                .limit(limit)
                .map(row -> {
                    Long productId = (Long) row[0];
                    Long totalSold = (Long) row[1];
                    
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product == null) return null;
                    
                    BigDecimal revenue = product.getPrice().multiply(BigDecimal.valueOf(totalSold));
                    
                    return new DashboardStats.TopSellingProduct(
                        productId,
                        product.getName(),
                        product.getMainImage(),
                        totalSold,
                        revenue
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private List<DashboardStats.TopRevenueProduct> getTopRevenueProducts(int limit) {
        // Simplified - same as top selling for now
        return getTopSellingProducts(limit).stream()
                .map(p -> new DashboardStats.TopRevenueProduct(
                    p.getProductId(),
                    p.getProductName(),
                    p.getProductImage(),
                    p.getRevenue(),
                    p.getTotalSold()
                ))
                .collect(Collectors.toList());
    }
    
    private List<DashboardStats.RecentOrder> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Order> orders = orderRepository.findRecentOrdersByUserId(null, pageable);
        
        return orders.stream()
                .map(order -> new DashboardStats.RecentOrder(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getUser().getName(),
                    order.getUser().getEmail(),
                    order.getTotalAmount(),
                    order.getStatus().name(),
                    order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .collect(Collectors.toList());
    }
    
    private List<SalesAnalytics.SalesDataPoint> getSalesByDay(List<Order> orders) {
        Map<String, SalesAnalytics.SalesDataPoint> dailySales = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getPaymentStatus() != Order.PaymentStatus.PAID) continue;
            
            String day = order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            dailySales.putIfAbsent(day, new SalesAnalytics.SalesDataPoint(
                day, BigDecimal.ZERO, 0L, 0L
            ));
            
            SalesAnalytics.SalesDataPoint point = dailySales.get(day);
            point.setRevenue(point.getRevenue().add(order.getTotalAmount()));
            point.setOrders(point.getOrders() + 1);
            point.setItems(point.getItems() + order.getOrderItems().size());
        }
        
        return new ArrayList<>(dailySales.values());
    }
    
    private List<SalesAnalytics.CategorySales> getSalesByCategory(List<Order> orders) {
        // Simplified implementation
        return new ArrayList<>();
    }
    
    private Map<String, BigDecimal> getSalesByPaymentMethod(List<Order> orders) {
        Map<String, BigDecimal> sales = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getPaymentStatus() != Order.PaymentStatus.PAID) continue;
            if (order.getPaymentMethod() == null) continue;
            
            String method = order.getPaymentMethod().name();
            sales.put(method, sales.getOrDefault(method, BigDecimal.ZERO).add(order.getTotalAmount()));
        }
        
        return sales;
    }
    
    private Map<String, Long> getOrdersByStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getStatus().name(),
                    Collectors.counting()
                ));
    }
    
    private List<SalesAnalytics.TopCustomer> getTopCustomers(List<Order> orders, int limit) {
        Map<Long, SalesAnalytics.TopCustomer> customers = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getPaymentStatus() != Order.PaymentStatus.PAID) continue;
            
            Long userId = order.getUser().getId();
            
            customers.putIfAbsent(userId, new SalesAnalytics.TopCustomer(
                userId,
                order.getUser().getName(),
                order.getUser().getEmail(),
                0L,
                BigDecimal.ZERO,
                null
            ));
            
            SalesAnalytics.TopCustomer customer = customers.get(userId);
            customer.setTotalOrders(customer.getTotalOrders() + 1);
            customer.setTotalSpent(customer.getTotalSpent().add(order.getTotalAmount()));
            customer.setLastOrderDate(order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        return customers.values().stream()
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}

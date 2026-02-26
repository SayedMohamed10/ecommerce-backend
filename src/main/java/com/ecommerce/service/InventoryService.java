package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class InventoryService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StockHistoryRepository stockHistoryRepository;
    
    @Autowired
    private StockAlertRepository stockAlertRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    
    // ==================== STOCK MANAGEMENT ====================
    
    /**
     * Update product stock
     */
    @Transactional
    public StockHistory updateStock(Long productId, Integer newStock, StockHistory.StockChangeType changeType, 
                                     String reason, String reference, Long adminId) {
        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Get admin user
        User admin = adminId != null ? userRepository.findById(adminId).orElse(null) : null;
        
        // Record history
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setPreviousStock(product.getStock());
        history.setNewStock(newStock);
        history.setChangeAmount(newStock - product.getStock());
        history.setChangeType(changeType);
        history.setReason(reason);
        history.setReference(reference);
        history.setChangedBy(admin);
        
        stockHistoryRepository.save(history);
        
        // Update product stock
        product.setStock(newStock);
        productRepository.save(product);
        
        // Check for alerts
        checkAndCreateStockAlerts(product);
        
        return history;
    }
    
    /**
     * Add stock (purchase, restock)
     */
    @Transactional
    public StockHistory addStock(Long productId, Integer quantity, String reason, String reference, Long adminId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        int newStock = product.getStock() + quantity;
        return updateStock(productId, newStock, StockHistory.StockChangeType.PURCHASE, reason, reference, adminId);
    }
    
    /**
     * Remove stock (sale, damage, loss)
     */
    @Transactional
    public StockHistory removeStock(Long productId, Integer quantity, StockHistory.StockChangeType changeType, 
                                     String reason, String reference, Long adminId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        int newStock = Math.max(0, product.getStock() - quantity);
        return updateStock(productId, newStock, changeType, reason, reference, adminId);
    }
    
    /**
     * Bulk update stock
     */
    @Transactional
    public List<StockHistory> bulkUpdateStock(Map<Long, Integer> productStocks, StockHistory.StockChangeType changeType, 
                                                String reason, Long adminId) {
        List<StockHistory> histories = new java.util.ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : productStocks.entrySet()) {
            Long productId = entry.getKey();
            Integer newStock = entry.getValue();
            
            try {
                StockHistory history = updateStock(productId, newStock, changeType, reason, null, adminId);
                histories.add(history);
            } catch (Exception e) {
                // Log error but continue with other products
                System.err.println("Failed to update stock for product " + productId + ": " + e.getMessage());
            }
        }
        
        return histories;
    }
    
    // ==================== STOCK HISTORY ====================
    
    /**
     * Get stock history for product
     */
    public Page<StockHistory> getProductStockHistory(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return stockHistoryRepository.findByProductId(productId, pageable);
    }
    
    /**
     * Get recent stock changes (all products)
     */
    public List<StockHistory> getRecentStockChanges(int limit) {
        return stockHistoryRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    /**
     * Get stock history by date range
     */
    public List<StockHistory> getStockHistoryByDateRange(LocalDateTime start, LocalDateTime end) {
        return stockHistoryRepository.findByCreatedAtBetween(start, end);
    }
    
    /**
     * Get stock history by change type
     */
    public Page<StockHistory> getStockHistoryByType(StockHistory.StockChangeType changeType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return stockHistoryRepository.findByChangeType(changeType, pageable);
    }
    
    // ==================== STOCK ALERTS ====================
    
    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        if (threshold == null) {
            threshold = DEFAULT_LOW_STOCK_THRESHOLD;
        }
        return productRepository.findByStockLessThan(threshold);
    }
    
    /**
     * Get out of stock products
     */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStock(0);
    }
    
    /**
     * Get active stock alerts
     */
    public Page<StockAlert> getActiveAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return stockAlertRepository.findByStatusOrderByCreatedAtDesc(StockAlert.AlertStatus.ACTIVE, pageable);
    }
    
    /**
     * Get alerts by type
     */
    public List<StockAlert> getAlertsByType(StockAlert.AlertType alertType) {
        return stockAlertRepository.findByAlertTypeAndStatus(alertType, StockAlert.AlertStatus.ACTIVE);
    }
    
    /**
     * Acknowledge alert
     */
    @Transactional
    public StockAlert acknowledgeAlert(Long alertId, Long adminId) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        
        alert.setStatus(StockAlert.AlertStatus.ACKNOWLEDGED);
        return stockAlertRepository.save(alert);
    }
    
    /**
     * Resolve alert
     */
    @Transactional
    public StockAlert resolveAlert(Long alertId, Long adminId, String notes) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        
        User admin = userRepository.findById(adminId).orElse(null);
        
        alert.setStatus(StockAlert.AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(admin);
        alert.setResolutionNotes(notes);
        
        return stockAlertRepository.save(alert);
    }
    
    /**
     * Dismiss alert
     */
    @Transactional
    public StockAlert dismissAlert(Long alertId) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        
        alert.setStatus(StockAlert.AlertStatus.DISMISSED);
        return stockAlertRepository.save(alert);
    }
    
    /**
     * Check and create stock alerts
     */
    @Transactional
    public void checkAndCreateStockAlerts(Product product) {
        // Check if alert already exists
        boolean alertExists = stockAlertRepository.existsByProductIdAndStatus(
            product.getId(), StockAlert.AlertStatus.ACTIVE
        );
        
        if (alertExists) {
            return; // Alert already exists
        }
        
        // Check for out of stock
        if (product.getStock() == 0) {
            createStockAlert(product, StockAlert.AlertType.OUT_OF_STOCK, 
                "Product is out of stock", 0);
        }
        // Check for low stock
        else if (product.getStock() < DEFAULT_LOW_STOCK_THRESHOLD) {
            createStockAlert(product, StockAlert.AlertType.LOW_STOCK, 
                "Product stock is low (below " + DEFAULT_LOW_STOCK_THRESHOLD + ")", 
                DEFAULT_LOW_STOCK_THRESHOLD);
        }
    }
    
    /**
     * Create stock alert
     */
    private void createStockAlert(Product product, StockAlert.AlertType alertType, 
                                   String message, Integer threshold) {
        StockAlert alert = new StockAlert();
        alert.setProduct(product);
        alert.setAlertType(alertType);
        alert.setCurrentStock(product.getStock());
        alert.setThreshold(threshold);
        alert.setMessage(message);
        alert.setStatus(StockAlert.AlertStatus.ACTIVE);
        
        stockAlertRepository.save(alert);
    }
    
    /**
     * Scan all products for stock alerts
     */
    @Transactional
    public void scanAllProductsForAlerts() {
        List<Product> allProducts = productRepository.findAll();
        
        for (Product product : allProducts) {
            if (product.getActive()) {
                checkAndCreateStockAlerts(product);
            }
        }
    }
    
    // ==================== INVENTORY STATISTICS ====================
    
    /**
     * Get inventory statistics
     */
    public InventoryStats getInventoryStats() {
        InventoryStats stats = new InventoryStats();
        
        stats.setTotalProducts(productRepository.count());
        stats.setActiveProducts(productRepository.countByActiveTrue());
        stats.setLowStockProducts(productRepository.countByStockLessThan(DEFAULT_LOW_STOCK_THRESHOLD));
        stats.setOutOfStockProducts(productRepository.countByStock(0));
        stats.setActiveAlerts(stockAlertRepository.countByStatus(StockAlert.AlertStatus.ACTIVE));
        
        // Total stock value (simplified)
        stats.setTotalStockValue(calculateTotalStockValue());
        
        return stats;
    }
    
    private Long calculateTotalStockValue() {
        // Simplified calculation - in production, multiply stock * price for each product
        return 0L; // Placeholder
    }
    
    // Inner class for inventory statistics
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class InventoryStats {
        private Long totalProducts;
        private Long activeProducts;
        private Long lowStockProducts;
        private Long outOfStockProducts;
        private Long activeAlerts;
        private Long totalStockValue;
    }
}

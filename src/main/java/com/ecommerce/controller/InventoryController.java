package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.StockAlert;
import com.ecommerce.model.StockHistory;
import com.ecommerce.model.User;
import com.ecommerce.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    // ==================== STOCK MANAGEMENT ====================
    
    /**
     * Update product stock
     * PUT /api/inventory/products/{id}/stock
     */
    @PutMapping("/products/{id}/stock")
    public ResponseEntity<StockHistory> updateStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        User admin = getUserFromUserDetails(userDetails);
        
        Integer newStock = Integer.parseInt(body.get("newStock").toString());
        String changeType = (String) body.get("changeType");
        String reason = (String) body.get("reason");
        String reference = (String) body.get("reference");
        
        StockHistory history = inventoryService.updateStock(
            id, newStock, 
            StockHistory.StockChangeType.valueOf(changeType),
            reason, reference, admin.getId()
        );
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Add stock
     * POST /api/inventory/products/{id}/add-stock
     */
    @PostMapping("/products/{id}/add-stock")
    public ResponseEntity<StockHistory> addStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        User admin = getUserFromUserDetails(userDetails);
        
        Integer quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = (String) body.get("reason");
        String reference = (String) body.get("reference");
        
        StockHistory history = inventoryService.addStock(id, quantity, reason, reference, admin.getId());
        
        return new ResponseEntity<>(history, HttpStatus.CREATED);
    }
    
    /**
     * Remove stock
     * POST /api/inventory/products/{id}/remove-stock
     */
    @PostMapping("/products/{id}/remove-stock")
    public ResponseEntity<StockHistory> removeStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        User admin = getUserFromUserDetails(userDetails);
        
        Integer quantity = Integer.parseInt(body.get("quantity").toString());
        String changeType = (String) body.get("changeType");
        String reason = (String) body.get("reason");
        String reference = (String) body.get("reference");
        
        StockHistory history = inventoryService.removeStock(
            id, quantity,
            StockHistory.StockChangeType.valueOf(changeType),
            reason, reference, admin.getId()
        );
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Bulk update stock
     * POST /api/inventory/bulk-update
     */
    @PostMapping("/bulk-update")
    public ResponseEntity<List<StockHistory>> bulkUpdateStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        
        User admin = getUserFromUserDetails(userDetails);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> productStocksRaw = (Map<String, Object>) body.get("productStocks");
        
        Map<Long, Integer> productStocks = new HashMap<>();
        for (Map.Entry<String, Object> entry : productStocksRaw.entrySet()) {
            productStocks.put(Long.parseLong(entry.getKey()), Integer.parseInt(entry.getValue().toString()));
        }
        
        String changeType = (String) body.get("changeType");
        String reason = (String) body.get("reason");
        
        List<StockHistory> histories = inventoryService.bulkUpdateStock(
            productStocks,
            StockHistory.StockChangeType.valueOf(changeType),
            reason, admin.getId()
        );
        
        return ResponseEntity.ok(histories);
    }
    
    // ==================== STOCK HISTORY ====================
    
    /**
     * Get stock history for product
     * GET /api/inventory/products/{id}/history
     */
    @GetMapping("/products/{id}/history")
    public ResponseEntity<Page<StockHistory>> getProductStockHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<StockHistory> history = inventoryService.getProductStockHistory(id, page, size);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get recent stock changes (all products)
     * GET /api/inventory/recent-changes
     */
    @GetMapping("/recent-changes")
    public ResponseEntity<List<StockHistory>> getRecentStockChanges(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<StockHistory> changes = inventoryService.getRecentStockChanges(limit);
        return ResponseEntity.ok(changes);
    }
    
    /**
     * Get stock history by date range
     * GET /api/inventory/history?start=X&end=Y
     */
    @GetMapping("/history")
    public ResponseEntity<List<StockHistory>> getStockHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<StockHistory> history = inventoryService.getStockHistoryByDateRange(start, end);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get stock history by change type
     * GET /api/inventory/history/type/{type}
     */
    @GetMapping("/history/type/{type}")
    public ResponseEntity<Page<StockHistory>> getStockHistoryByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<StockHistory> history = inventoryService.getStockHistoryByType(
            StockHistory.StockChangeType.valueOf(type), page, size
        );
        return ResponseEntity.ok(history);
    }
    
    // ==================== STOCK ALERTS ====================
    
    /**
     * Get low stock products
     * GET /api/inventory/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(required = false) Integer threshold) {
        
        List<Product> products = inventoryService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    /**
     * Get out of stock products
     * GET /api/inventory/out-of-stock
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStockProducts() {
        List<Product> products = inventoryService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * Get active stock alerts
     * GET /api/inventory/alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<Page<StockAlert>> getActiveAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<StockAlert> alerts = inventoryService.getActiveAlerts(page, size);
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Get alerts by type
     * GET /api/inventory/alerts/type/{type}
     */
    @GetMapping("/alerts/type/{type}")
    public ResponseEntity<List<StockAlert>> getAlertsByType(@PathVariable String type) {
        List<StockAlert> alerts = inventoryService.getAlertsByType(
            StockAlert.AlertType.valueOf(type)
        );
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * Acknowledge alert
     * PUT /api/inventory/alerts/{id}/acknowledge
     */
    @PutMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<StockAlert> acknowledgeAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User admin = getUserFromUserDetails(userDetails);
        StockAlert alert = inventoryService.acknowledgeAlert(id, admin.getId());
        return ResponseEntity.ok(alert);
    }
    
    /**
     * Resolve alert
     * PUT /api/inventory/alerts/{id}/resolve
     */
    @PutMapping("/alerts/{id}/resolve")
    public ResponseEntity<StockAlert> resolveAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        User admin = getUserFromUserDetails(userDetails);
        String notes = body.get("notes");
        
        StockAlert alert = inventoryService.resolveAlert(id, admin.getId(), notes);
        return ResponseEntity.ok(alert);
    }
    
    /**
     * Dismiss alert
     * PUT /api/inventory/alerts/{id}/dismiss
     */
    @PutMapping("/alerts/{id}/dismiss")
    public ResponseEntity<StockAlert> dismissAlert(@PathVariable Long id) {
        StockAlert alert = inventoryService.dismissAlert(id);
        return ResponseEntity.ok(alert);
    }
    
    /**
     * Scan all products for alerts
     * POST /api/inventory/scan-alerts
     */
    @PostMapping("/scan-alerts")
    public ResponseEntity<Map<String, String>> scanForAlerts() {
        inventoryService.scanAllProductsForAlerts();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Alert scan completed successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== STATISTICS ====================
    
    /**
     * Get inventory statistics
     * GET /api/inventory/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<InventoryService.InventoryStats> getInventoryStats() {
        InventoryService.InventoryStats stats = inventoryService.getInventoryStats();
        return ResponseEntity.ok(stats);
    }
    
    // Helper method
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

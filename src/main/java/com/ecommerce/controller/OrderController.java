package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    // ==================== USER ENDPOINTS ====================
    
    /**
     * Create order from cart
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        OrderResponse order = orderService.createOrder(user.getId(), request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
    
    /**
     * Get order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User user = getUserFromUserDetails(userDetails);
        OrderResponse order = orderService.getOrder(user.getId(), id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get order by order number
     * GET /api/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber) {
        
        User user = getUserFromUserDetails(userDetails);
        OrderResponse order = orderService.getOrderByNumber(user.getId(), orderNumber);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get order history
     * GET /api/orders?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getUserFromUserDetails(userDetails);
        Page<OrderResponse> orders = orderService.getOrderHistory(user.getId(), page, size);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get recent orders (last 5)
     * GET /api/orders/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        List<OrderResponse> orders = orderService.getRecentOrders(user.getId());
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Cancel order
     * POST /api/orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        
        User user = getUserFromUserDetails(userDetails);
        String reason = body != null ? body.get("reason") : "Cancelled by user";
        OrderResponse order = orderService.cancelOrder(user.getId(), id, reason);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get user order statistics
     * GET /api/orders/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<OrderService.OrderStatistics> getUserStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        OrderService.OrderStatistics stats = orderService.getUserOrderStatistics(user.getId());
        return ResponseEntity.ok(stats);
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Update order status (Admin)
     * PUT /api/orders/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String status = body.get("status");
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Update payment status (Admin)
     * PUT /api/orders/{id}/payment-status
     */
    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String paymentStatus = body.get("paymentStatus");
        OrderResponse order = orderService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Add tracking number (Admin)
     * PUT /api/orders/{id}/tracking
     */
    @PutMapping("/{id}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> addTrackingNumber(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String trackingNumber = body.get("trackingNumber");
        OrderResponse order = orderService.addTrackingNumber(id, trackingNumber);
        return ResponseEntity.ok(order);
    }
    
    // Helper method to get User from UserDetails
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

package com.ecommerce.controller;

import com.ecommerce.dto.CreateCouponRequest;
import com.ecommerce.dto.CouponResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    // ==================== PUBLIC ENDPOINTS ====================
    
    /**
     * Validate coupon
     * POST /api/coupons/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponResponse> validateCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        
        User user = getUserFromUserDetails(userDetails);
        
        String code = (String) request.get("code");
        BigDecimal orderTotal = new BigDecimal(request.get("orderTotal").toString());
        
        @SuppressWarnings("unchecked")
        List<Long> productIds = (List<Long>) request.get("productIds");
        
        CouponResponse response = couponService.validateCoupon(code, user.getId(), orderTotal, productIds);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get valid coupons (currently usable)
     * GET /api/coupons/valid
     */
    @GetMapping("/valid")
    public ResponseEntity<List<CouponResponse>> getValidCoupons() {
        List<CouponResponse> coupons = couponService.getValidCoupons();
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * Get coupon by code (public info)
     * GET /api/coupons/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        CouponResponse coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Create coupon (Admin)
     * POST /api/coupons
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse coupon = couponService.createCoupon(request);
        return new ResponseEntity<>(coupon, HttpStatus.CREATED);
    }
    
    /**
     * Update coupon (Admin)
     * PUT /api/coupons/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CreateCouponRequest request) {
        
        CouponResponse coupon = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(coupon);
    }
    
    /**
     * Get all coupons (Admin)
     * GET /api/coupons
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<CouponResponse> coupons = couponService.getAllCoupons(page, size);
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * Get active coupons (Admin)
     * GET /api/coupons/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CouponResponse>> getActiveCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<CouponResponse> coupons = couponService.getActiveCoupons(page, size);
        return ResponseEntity.ok(coupons);
    }
    
    /**
     * Get coupon by ID (Admin)
     * GET /api/coupons/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> getCoupon(@PathVariable Long id) {
        CouponResponse coupon = couponService.getCoupon(id);
        return ResponseEntity.ok(coupon);
    }
    
    /**
     * Delete coupon (Admin)
     * DELETE /api/coupons/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deactivate coupon (Admin)
     * PUT /api/coupons/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> deactivateCoupon(@PathVariable Long id) {
        CouponResponse coupon = couponService.deactivateCoupon(id);
        return ResponseEntity.ok(coupon);
    }
    
    /**
     * Activate coupon (Admin)
     * PUT /api/coupons/{id}/activate
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> activateCoupon(@PathVariable Long id) {
        CouponResponse coupon = couponService.activateCoupon(id);
        return ResponseEntity.ok(coupon);
    }
    
    // Helper method
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

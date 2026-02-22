package com.ecommerce.service;

import com.ecommerce.dto.CreateCouponRequest;
import com.ecommerce.dto.CouponResponse;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {
    
    @Autowired
    private CouponRepository couponRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create coupon (Admin)
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalStateException("Coupon code already exists");
        }
        
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDescription(request.getDescription());
        
        // Set discount type
        try {
            coupon.setDiscountType(Coupon.DiscountType.valueOf(request.getDiscountType()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid discount type. Must be PERCENTAGE or FIXED_AMOUNT");
        }
        
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumPurchase(request.getMinimumPurchase() != null ? request.getMinimumPurchase() : BigDecimal.ZERO);
        coupon.setMaximumDiscount(request.getMaximumDiscount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setUsageLimitPerUser(request.getUsageLimitPerUser());
        coupon.setStartsAt(request.getStartsAt());
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setActive(request.getActive());
        coupon.setFirstPurchaseOnly(request.getFirstPurchaseOnly());
        
        // Set category if specified
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            coupon.setCategory(category);
        }
        
        // Set product if specified
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            coupon.setProduct(product);
        }
        
        coupon = couponRepository.save(coupon);
        
        return mapToResponse(coupon);
    }
    
    // Update coupon (Admin)
    @Transactional
    public CouponResponse updateCoupon(Long id, CreateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        // Check if code is being changed and already exists
        if (!coupon.getCode().equals(request.getCode().toUpperCase())) {
            if (couponRepository.existsByCode(request.getCode())) {
                throw new IllegalStateException("Coupon code already exists");
            }
            coupon.setCode(request.getCode().toUpperCase());
        }
        
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(Coupon.DiscountType.valueOf(request.getDiscountType()));
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumPurchase(request.getMinimumPurchase() != null ? request.getMinimumPurchase() : BigDecimal.ZERO);
        coupon.setMaximumDiscount(request.getMaximumDiscount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setUsageLimitPerUser(request.getUsageLimitPerUser());
        coupon.setStartsAt(request.getStartsAt());
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setActive(request.getActive());
        coupon.setFirstPurchaseOnly(request.getFirstPurchaseOnly());
        
        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            coupon.setCategory(category);
        } else {
            coupon.setCategory(null);
        }
        
        // Update product
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            coupon.setProduct(product);
        } else {
            coupon.setProduct(null);
        }
        
        coupon = couponRepository.save(coupon);
        
        return mapToResponse(coupon);
    }
    
    // Validate and apply coupon
    @Transactional
    public CouponResponse validateCoupon(String code, Long userId, BigDecimal orderTotal, List<Long> productIds) {
        // Find coupon
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code"));
        
        CouponResponse response = mapToResponse(coupon);
        
        // Check if active
        if (!coupon.getActive()) {
            response.setValid(false);
            response.setValidationMessage("This coupon is no longer active");
            return response;
        }
        
        // Check if expired
        if (coupon.isExpired()) {
            response.setValid(false);
            response.setValidationMessage("This coupon has expired");
            return response;
        }
        
        // Check if not started
        if (coupon.isNotStarted()) {
            response.setValid(false);
            response.setValidationMessage("This coupon is not yet valid. Starts at: " + coupon.getStartsAt());
            return response;
        }
        
        // Check usage limit
        if (coupon.hasReachedUsageLimit()) {
            response.setValid(false);
            response.setValidationMessage("This coupon has reached its usage limit");
            return response;
        }
        
        // Check minimum purchase
        if (orderTotal.compareTo(coupon.getMinimumPurchase()) < 0) {
            response.setValid(false);
            response.setValidationMessage("Minimum purchase of $" + coupon.getMinimumPurchase() + " required");
            return response;
        }
        
        // Check first purchase only
        if (coupon.getFirstPurchaseOnly() != null && coupon.getFirstPurchaseOnly()) {
            Long userOrderCount = orderRepository.countByUserId(userId);
            if (userOrderCount > 0) {
                response.setValid(false);
                response.setValidationMessage("This coupon is only valid for first-time purchases");
                return response;
            }
        }
        
        // Check category restriction
        if (coupon.getCategory() != null && productIds != null && !productIds.isEmpty()) {
            boolean hasMatchingCategory = productRepository.findAllById(productIds).stream()
                    .anyMatch(p -> p.getCategory().getId().equals(coupon.getCategory().getId()));
            
            if (!hasMatchingCategory) {
                response.setValid(false);
                response.setValidationMessage("This coupon is only valid for products in category: " + coupon.getCategory().getName());
                return response;
            }
        }
        
        // Check product restriction
        if (coupon.getProduct() != null && productIds != null && !productIds.isEmpty()) {
            boolean hasMatchingProduct = productIds.contains(coupon.getProduct().getId());
            
            if (!hasMatchingProduct) {
                response.setValid(false);
                response.setValidationMessage("This coupon is only valid for product: " + coupon.getProduct().getName());
                return response;
            }
        }
        
        // Calculate discount
        BigDecimal discount = coupon.calculateDiscount(orderTotal);
        
        response.setValid(true);
        response.setValidationMessage("Coupon applied successfully!");
        response.setDiscountAmount(discount);
        
        return response;
    }
    
    // Apply coupon to order (increment usage count)
    @Transactional
    public void applyCoupon(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        coupon.incrementUsageCount();
        couponRepository.save(coupon);
    }
    
    // Get coupon by ID
    public CouponResponse getCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        return mapToResponse(coupon);
    }
    
    // Get coupon by code
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        return mapToResponse(coupon);
    }
    
    // Get all coupons (Admin)
    public Page<CouponResponse> getAllCoupons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Coupon> coupons = couponRepository.findAll(pageable);
        return coupons.map(this::mapToResponse);
    }
    
    // Get active coupons (Admin)
    public Page<CouponResponse> getActiveCoupons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Coupon> coupons = couponRepository.findByActiveTrue(pageable);
        return coupons.map(this::mapToResponse);
    }
    
    // Get valid coupons (currently usable)
    public List<CouponResponse> getValidCoupons() {
        List<Coupon> coupons = couponRepository.findValidCoupons(LocalDateTime.now());
        return coupons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Delete coupon (Admin)
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        couponRepository.delete(coupon);
    }
    
    // Deactivate coupon (Admin)
    @Transactional
    public CouponResponse deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        coupon.setActive(false);
        coupon = couponRepository.save(coupon);
        
        return mapToResponse(coupon);
    }
    
    // Activate coupon (Admin)
    @Transactional
    public CouponResponse activateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        coupon.setActive(true);
        coupon = couponRepository.save(coupon);
        
        return mapToResponse(coupon);
    }
    
    // Map Coupon to CouponResponse
    private CouponResponse mapToResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDescription(coupon.getDescription());
        response.setDiscountType(coupon.getDiscountType().name());
        response.setDiscountValue(coupon.getDiscountValue());
        response.setMinimumPurchase(coupon.getMinimumPurchase());
        response.setMaximumDiscount(coupon.getMaximumDiscount());
        response.setUsageLimit(coupon.getUsageLimit());
        response.setUsageCount(coupon.getUsageCount());
        response.setUsageLimitPerUser(coupon.getUsageLimitPerUser());
        response.setStartsAt(coupon.getStartsAt());
        response.setExpiresAt(coupon.getExpiresAt());
        response.setActive(coupon.getActive());
        response.setFirstPurchaseOnly(coupon.getFirstPurchaseOnly());
        response.setCreatedAt(coupon.getCreatedAt());
        response.setUpdatedAt(coupon.getUpdatedAt());
        
        if (coupon.getCategory() != null) {
            response.setCategoryId(coupon.getCategory().getId());
            response.setCategoryName(coupon.getCategory().getName());
        }
        
        if (coupon.getProduct() != null) {
            response.setProductId(coupon.getProduct().getId());
            response.setProductName(coupon.getProduct().getName());
        }
        
        return response;
    }
}

package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, length = 200)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "minimum_purchase", precision = 10, scale = 2)
    private BigDecimal minimumPurchase = BigDecimal.ZERO;
    
    @Column(name = "maximum_discount", precision = 10, scale = 2)
    private BigDecimal maximumDiscount;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;
    
    @Column(name = "starts_at")
    private LocalDateTime startsAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Coupon valid for specific category
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // Coupon valid for specific product
    
    @Column(name = "first_purchase_only")
    private Boolean firstPurchaseOnly = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Discount Type Enum
    public enum DiscountType {
        PERCENTAGE,     // 10% off
        FIXED_AMOUNT    // $20 off
    }
    
    // Helper methods
    public void incrementUsageCount() {
        this.usageCount++;
    }
    
    public boolean isExpired() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isNotStarted() {
        if (startsAt == null) return false;
        return LocalDateTime.now().isBefore(startsAt);
    }
    
    public boolean hasReachedUsageLimit() {
        if (usageLimit == null) return false;
        return usageCount >= usageLimit;
    }
    
    public boolean isValid() {
        return active && !isExpired() && !isNotStarted() && !hasReachedUsageLimit();
    }
    
    public BigDecimal calculateDiscount(BigDecimal orderTotal) {
        BigDecimal discount;
        
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderTotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discount = discountValue;
        }
        
        // Apply maximum discount limit
        if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
            discount = maximumDiscount;
        }
        
        // Discount cannot exceed order total
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        
        return discount;
    }
}

package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    
    private Long id;
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumPurchase;
    private BigDecimal maximumDiscount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer usageLimitPerUser;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Boolean active;
    private Long categoryId;
    private String categoryName;
    private Long productId;
    private String productName;
    private Boolean firstPurchaseOnly;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Validation result fields (when validating)
    private Boolean valid;
    private String validationMessage;
    private BigDecimal discountAmount;
}

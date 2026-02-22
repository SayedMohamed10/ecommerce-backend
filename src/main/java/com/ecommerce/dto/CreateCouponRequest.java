package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {
    
    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, hyphens, and underscores")
    private String code;
    
    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must be less than 200 characters")
    private String description;
    
    @NotNull(message = "Discount type is required")
    private String discountType; // PERCENTAGE or FIXED_AMOUNT
    
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.00", message = "Minimum purchase must be 0 or greater")
    private BigDecimal minimumPurchase;
    
    @DecimalMin(value = "0.01", message = "Maximum discount must be greater than 0")
    private BigDecimal maximumDiscount;
    
    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;
    
    @Min(value = 1, message = "Usage limit per user must be at least 1")
    private Integer usageLimitPerUser;
    
    private LocalDateTime startsAt;
    
    private LocalDateTime expiresAt;
    
    private Boolean active = true;
    
    private Long categoryId; // Restrict to category
    
    private Long productId; // Restrict to product
    
    private Boolean firstPurchaseOnly = false;
}

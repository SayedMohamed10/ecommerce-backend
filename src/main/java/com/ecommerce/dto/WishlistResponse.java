package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productSku;
    private String productDescription;
    private BigDecimal productPrice;
    private BigDecimal productDiscountPrice;
    private String productMainImage;
    private Boolean productInStock;
    private Integer productStock;
    private Boolean productActive;
    private String notes;
    private LocalDateTime addedAt;
    
    // Calculated fields
    private Boolean hasDiscount;
    private BigDecimal discountPercentage;
    private Boolean isAvailable; // In stock AND active
}

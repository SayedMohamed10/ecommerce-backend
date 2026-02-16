package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stock;
    private Boolean active;
    private Boolean featured;
    private CategorySummary category;
    private List<String> images;
    private List<String> tags;
    private String brand;
    private String sku;
    private BigDecimal weight;
    private String dimensions;
    private Double averageRating;
    private Integer reviewCount;
    private Long viewCount;
    private Integer soldCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private Boolean inStock;
    private Boolean hasDiscount;
    private Integer discountPercentage;
    private BigDecimal effectivePrice;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private Long id;
        private String name;
        private String imageUrl;
    }
}

package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Discount price must be non-negative")
    private BigDecimal discountPrice;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;
    
    private Boolean active = true;
    
    private Boolean featured = false;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    private List<String> images = new ArrayList<>();
    
    private List<String> tags = new ArrayList<>();
    
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brand;
    
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;
    
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    private BigDecimal weight;
    
    @Size(max = 500, message = "Dimensions must not exceed 500 characters")
    private String dimensions;
    
    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;
    
    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;
    
    @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
    private String metaKeywords;
}

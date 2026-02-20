package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String slug;
    
    @Column(length = 2000)
    private String description;
    
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Discount price must be non-negative")
    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;
    
    @Min(value = 0, message = "Stock must be non-negative")
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Boolean featured = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 50)
    private String sku;
    
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    @Column(precision = 8, scale = 2)
    private BigDecimal weight;
    
    @Column(length = 500)
    private String dimensions;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    @Column(name = "view_count")
    private Long viewCount = 0L;
    
    @Column(name = "sold_count")
    private Integer soldCount = 0;
    
    @Column(name = "meta_title", length = 255)
    private String metaTitle;
    
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isInStock() {
        return stock != null && stock > 0;
    }
    
    public boolean hasDiscount() {
        return discountPrice != null && discountPrice.compareTo(price) < 0;
    }
    
    public BigDecimal getEffectivePrice() {
        return hasDiscount() ? discountPrice : price;
    }
    
    public Integer getDiscountPercentage() {
        if (!hasDiscount()) {
            return 0;
        }
        BigDecimal discount = price.subtract(discountPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(price, 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }
    
    public String getMainImage() {
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }
    
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1;
    }
    
    public void incrementSoldCount(Integer quantity) {
        this.soldCount = (this.soldCount == null ? 0 : this.soldCount) + quantity;
    }
    
    public void decrementStock(Integer quantity) {
        if (this.stock >= quantity) {
            this.stock -= quantity;
        }
    }
    
    public void updateRating(Double newRating, Integer newReviewCount) {
        this.averageRating = newRating;
        this.reviewCount = newReviewCount;
    }
}

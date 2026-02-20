package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewSummary {
    
    private Long productId;
    private String productName;
    private Long totalReviews;
    private Double averageRating;
    private Map<Integer, Long> ratingDistribution = new HashMap<>();
    private Long verifiedPurchaseCount;
    private Long reviewsWithImages;
    
    // Rating percentage
    private Double fiveStarPercentage;
    private Double fourStarPercentage;
    private Double threeStarPercentage;
    private Double twoStarPercentage;
    private Double oneStarPercentage;
    
    // Helper method to calculate percentages
    public void calculatePercentages() {
        if (totalReviews == 0) {
            fiveStarPercentage = 0.0;
            fourStarPercentage = 0.0;
            threeStarPercentage = 0.0;
            twoStarPercentage = 0.0;
            oneStarPercentage = 0.0;
            return;
        }
        
        fiveStarPercentage = (ratingDistribution.getOrDefault(5, 0L) * 100.0) / totalReviews;
        fourStarPercentage = (ratingDistribution.getOrDefault(4, 0L) * 100.0) / totalReviews;
        threeStarPercentage = (ratingDistribution.getOrDefault(3, 0L) * 100.0) / totalReviews;
        twoStarPercentage = (ratingDistribution.getOrDefault(2, 0L) * 100.0) / totalReviews;
        oneStarPercentage = (ratingDistribution.getOrDefault(1, 0L) * 100.0) / totalReviews;
    }
}

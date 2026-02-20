package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    
    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean verifiedPurchase;
    private List<String> images = new ArrayList<>();
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private String status;
    private String adminResponse;
    private LocalDateTime adminResponseAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User can mark as helpful
    private Boolean markedHelpfulByCurrentUser = false;
}

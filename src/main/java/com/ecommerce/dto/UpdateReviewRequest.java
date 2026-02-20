package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    @NotBlank(message = "Review title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @NotBlank(message = "Review comment is required")
    @Size(min = 20, max = 2000, message = "Comment must be between 20 and 2000 characters")
    private String comment;
    
    @Size(max = 5, message = "Maximum 5 images allowed")
    private List<String> images;
}

package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToWishlistRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;
}

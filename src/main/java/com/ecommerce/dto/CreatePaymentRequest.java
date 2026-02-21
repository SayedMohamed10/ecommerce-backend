package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    private String paymentMethodId; // Stripe payment method ID
    
    private String currency = "USD";
}

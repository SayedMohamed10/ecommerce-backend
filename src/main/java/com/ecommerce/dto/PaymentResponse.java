package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String cardLast4;
    private String cardBrand;
    private String receiptUrl;
    private String failureMessage;
    private Boolean refunded;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    // For Stripe checkout
    private String clientSecret; // Stripe client secret for frontend
    private String paymentIntentId;
}

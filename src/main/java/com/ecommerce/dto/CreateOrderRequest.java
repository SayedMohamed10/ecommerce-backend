package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    // Shipping Information
    @NotBlank(message = "Shipping name is required")
    @Size(max = 100)
    private String shippingName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String shippingEmail;
    
    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String shippingPhone;
    
    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String shippingAddressLine1;
    
    @Size(max = 255)
    private String shippingAddressLine2;
    
    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String shippingCity;
    
    @Size(max = 100)
    private String shippingState;
    
    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    private String shippingPostalCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String shippingCountry;
    
    // Payment Information
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, STRIPE, CASH_ON_DELIVERY
    
    private String paymentTransactionId; // From payment gateway
    
    // Additional Info
    @Size(max = 1000)
    private String orderNotes;
}

package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    
    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address is too long")
    private String streetAddress;
    
    @Size(max = 100, message = "Apartment is too long")
    private String apartment;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City name is too long")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State name is too long")
    private String state;
    
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code is too long")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country name is too long")
    private String country;
    
    private Boolean isDefault = false;
    
    private String type = "SHIPPING"; // SHIPPING, BILLING, BOTH
}

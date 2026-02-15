package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "Street address is required")
    @Size(max = 255)
    @Column(name = "street_address", nullable = false)
    private String streetAddress;
    
    @Size(max = 100)
    private String apartment;
    
    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String state;
    
    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    @Column(name = "postal_code", nullable = false)
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String country;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType type = AddressType.SHIPPING;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum AddressType {
        SHIPPING,
        BILLING,
        BOTH
    }
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(streetAddress);
        if (apartment != null && !apartment.isEmpty()) {
            sb.append(", ").append(apartment);
        }
        sb.append(", ").append(city);
        sb.append(", ").append(state);
        sb.append(" ").append(postalCode);
        sb.append(", ").append(country);
        return sb.toString();
    }
}

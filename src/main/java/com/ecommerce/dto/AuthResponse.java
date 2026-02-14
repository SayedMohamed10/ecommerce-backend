package com.ecommerce.dto;

import com.ecommerce.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private Boolean requiresTwoFactor; // True if 2FA verification is needed
    
    public AuthResponse(String accessToken, String refreshToken, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.emailVerified = user.getEmailVerified();
        this.twoFactorEnabled = user.getTwoFactorEnabled();
        this.requiresTwoFactor = false;
    }
    
    public AuthResponse(String accessToken, User user) {
        this.accessToken = accessToken;
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.emailVerified = user.getEmailVerified();
        this.twoFactorEnabled = user.getTwoFactorEnabled();
        this.requiresTwoFactor = false;
    }
}

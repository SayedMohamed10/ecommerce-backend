package com.ecommerce.dto;

import com.ecommerce.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;

    // User info - using 'id' to match setId() calls in AuthService
    private Long id;
    private String name;
    private String email;
    private String role;
    private String message;

    // Status fields
    private Boolean success;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private Boolean requiresTwoFactor;

    // Constructor used by AuthService: new AuthResponse(accessToken, refreshToken, user)
    public AuthResponse(String accessToken, String refreshToken, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole() != null ? user.getRole().name() : "USER";
        this.emailVerified = user.getEmailVerified();
        this.twoFactorEnabled = user.getTwoFactorEnabled();
        this.success = true;
    }

    // Static factory methods
    public static AuthResponse success(String accessToken, String refreshToken, User user) {
        return new AuthResponse(accessToken, refreshToken, user);
    }

    public static AuthResponse error(String message) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static AuthResponse requiresTwoFactor() {
        AuthResponse response = new AuthResponse();
        response.setRequiresTwoFactor(true);
        response.setSuccess(false);
        response.setMessage("Two-factor authentication required");
        return response;
    }
}
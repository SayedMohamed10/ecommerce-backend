package com.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = true)
    private String password;
    
    @Column(name = "phone_number")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    // Email Verification
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    // Account Lockout
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "lockout_time")
    private LocalDateTime lockoutTime;
    
    // Two-Factor Authentication
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;
    
    // OAuth2
    @Column(name = "oauth_provider")
    private String oauthProvider;
    
    @Column(name = "oauth_id")
    private String oauthId;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    public enum Role {
        USER,
        ADMIN
    }
    
    // Helper methods for account lockout
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }
    
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockoutTime = null;
    }
    
    public void lockAccount() {
        this.accountLocked = true;
        this.lockoutTime = LocalDateTime.now();
    }
    
    public boolean isAccountLocked() {
        if (!accountLocked) {
            return false;
        }
        
        // Auto-unlock after 30 minutes
        if (lockoutTime != null && lockoutTime.plusMinutes(30).isBefore(LocalDateTime.now())) {
            resetFailedAttempts();
            return false;
        }
        
        return true;
    }
}

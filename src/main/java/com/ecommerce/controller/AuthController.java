package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.model.User;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // ==================== SIGNUP & LOGIN ====================
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.signup(signupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== REFRESH TOKEN ====================
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    // ==================== EMAIL VERIFICATION ====================
    
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@Valid @RequestBody PasswordResetRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Verification email sent successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== PASSWORD RESET ====================
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset email sent successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== TWO-FACTOR AUTHENTICATION ====================
    
    @PostMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupResponse> setup2FA(Authentication authentication) {
        String email = authentication.getName();
        User user = authService.getCurrentUser(email);
        
        TwoFactorSetupResponse response = authService.setup2FA(user);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/2fa/enable")
    public ResponseEntity<Map<String, String>> enable2FA(
            Authentication authentication,
            @RequestParam String secret,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        
        String email = authentication.getName();
        User user = authService.getCurrentUser(email);
        
        authService.enable2FA(user, secret, request.getCode());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Two-factor authentication enabled successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/2fa/disable")
    public ResponseEntity<Map<String, String>> disable2FA(
            Authentication authentication,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        
        String email = authentication.getName();
        User user = authService.getCurrentUser(email);
        
        authService.disable2FA(user, request.getCode());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Two-factor authentication disabled successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== VALIDATE TOKEN ====================
    
    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken(@RequestHeader("Authorization") String token) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Token is valid");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== CURRENT USER ====================
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = authService.getCurrentUser(email);
        
        // Remove sensitive data
        user.setPassword(null);
        user.setTwoFactorSecret(null);
        
        return ResponseEntity.ok(user);
    }
}

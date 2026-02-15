package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // ==================== USER PROFILE ====================
    
    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        
        // Remove sensitive data
        user.setPassword(null);
        user.setTwoFactorSecret(null);
        
        return ResponseEntity.ok(user);
    }
    
    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        String email = authentication.getName();
        User updatedUser = userService.updateProfile(email, request);
        
        // Remove sensitive data
        updatedUser.setPassword(null);
        updatedUser.setTwoFactorSecret(null);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Change password
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        String email = authentication.getName();
        userService.changePassword(email, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete account
     */
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            Authentication authentication,
            @RequestParam(required = false) String confirmation) {
        
        if (!"DELETE".equals(confirmation)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Please confirm account deletion by sending 'DELETE' as confirmation parameter");
            return ResponseEntity.badRequest().body(error);
        }
        
        String email = authentication.getName();
        userService.deleteAccount(email);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== DASHBOARD ====================
    
    /**
     * Get user dashboard with statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(Authentication authentication) {
        String email = authentication.getName();
        DashboardResponse dashboard = userService.getDashboard(email);
        
        return ResponseEntity.ok(dashboard);
    }
    
    // ==================== ADDRESS MANAGEMENT ====================
    
    /**
     * Get all user's addresses
     */
    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(Authentication authentication) {
        String email = authentication.getName();
        List<Address> addresses = userService.getUserAddresses(email);
        
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * Add new address
     */
    @PostMapping("/addresses")
    public ResponseEntity<Address> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {
        
        String email = authentication.getName();
        Address address = userService.addAddress(email, request);
        
        return new ResponseEntity<>(address, HttpStatus.CREATED);
    }
    
    /**
     * Update address
     */
    @PutMapping("/addresses/{id}")
    public ResponseEntity<Address> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        
        String email = authentication.getName();
        Address address = userService.updateAddress(email, id, request);
        
        return ResponseEntity.ok(address);
    }
    
    /**
     * Delete address
     */
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Map<String, String>> deleteAddress(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        userService.deleteAddress(email, id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Address deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== USER STATISTICS ====================
    
    /**
     * Get user statistics summary
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("memberSince", user.getCreatedAt());
        stats.put("emailVerified", user.getEmailVerified());
        stats.put("twoFactorEnabled", user.getTwoFactorEnabled());
        stats.put("lastLogin", user.getLastLogin());
        stats.put("accountLocked", user.getAccountLocked());
        
        return ResponseEntity.ok(stats);
    }
}

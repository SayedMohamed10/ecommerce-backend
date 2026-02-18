package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    /**
     * Get user's cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromUserDetails(userDetails);
        CartResponse cart = cartService.getCart(user.getId());
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Add item to cart
     * POST /api/cart
     * Body: { "productId": 1, "quantity": 2 }
     */
    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        CartResponse cart = cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Update cart item quantity
     * PUT /api/cart/{productId}
     * Body: { "quantity": 3 }
     */
    @PutMapping("/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        CartResponse cart = cartService.updateCartItem(user.getId(), productId, request);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Remove item from cart
     * DELETE /api/cart/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        
        User user = getUserFromUserDetails(userDetails);
        CartResponse cart = cartService.removeFromCart(user.getId(), productId);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Clear entire cart
     * DELETE /api/cart
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        cartService.clearCart(user.getId());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get cart item count
     * GET /api/cart/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCartItemCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        Long count = cartService.getCartItemCount(user.getId());
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate cart before checkout
     * GET /api/cart/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<CartService.CartValidationResponse> validateCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        CartService.CartValidationResponse validation = cartService.validateCart(user.getId());
        
        return ResponseEntity.ok(validation);
    }
    
    // Helper method to get User from UserDetails
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

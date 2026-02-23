package com.ecommerce.controller;

import com.ecommerce.dto.AddToWishlistRequest;
import com.ecommerce.dto.WishlistResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WishlistController {
    
    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    /**
     * Get user's wishlist
     * GET /api/wishlist
     */
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        List<WishlistResponse> wishlist = wishlistService.getWishlist(user.getId());
        return ResponseEntity.ok(wishlist);
    }
    
    /**
     * Get wishlist with pagination
     * GET /api/wishlist/paginated?page=0&size=10
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<WishlistResponse>> getWishlistPaginated(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getUserFromUserDetails(userDetails);
        Page<WishlistResponse> wishlist = wishlistService.getWishlistPaginated(user.getId(), page, size);
        return ResponseEntity.ok(wishlist);
    }
    
    /**
     * Add to wishlist
     * POST /api/wishlist
     */
    @PostMapping
    public ResponseEntity<WishlistResponse> addToWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToWishlistRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        WishlistResponse wishlist = wishlistService.addToWishlist(user.getId(), request);
        return new ResponseEntity<>(wishlist, HttpStatus.CREATED);
    }
    
    /**
     * Remove from wishlist
     * DELETE /api/wishlist/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> removeFromWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        
        User user = getUserFromUserDetails(userDetails);
        wishlistService.removeFromWishlist(user.getId(), productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product removed from wishlist");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get wishlist count
     * GET /api/wishlist/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWishlistCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        Long count = wishlistService.getWishlistCount(user.getId());
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if product is in wishlist
     * GET /api/wishlist/check/{productId}
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkInWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        
        User user = getUserFromUserDetails(userDetails);
        boolean inWishlist = wishlistService.isInWishlist(user.getId(), productId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("inWishlist", inWishlist);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Move to cart
     * POST /api/wishlist/{productId}/move-to-cart
     */
    @PostMapping("/{productId}/move-to-cart")
    public ResponseEntity<Map<String, String>> moveToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        
        User user = getUserFromUserDetails(userDetails);
        wishlistService.moveToCart(user.getId(), productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product moved to cart");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Move all to cart
     * POST /api/wishlist/move-all-to-cart
     */
    @PostMapping("/move-all-to-cart")
    public ResponseEntity<Map<String, String>> moveAllToCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        wishlistService.moveAllToCart(user.getId());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All items moved to cart");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Clear wishlist
     * DELETE /api/wishlist
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        wishlistService.clearWishlist(user.getId());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Wishlist cleared");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update notes
     * PUT /api/wishlist/{productId}/notes
     */
    @PutMapping("/{productId}/notes")
    public ResponseEntity<WishlistResponse> updateNotes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody Map<String, String> body) {
        
        User user = getUserFromUserDetails(userDetails);
        String notes = body.get("notes");
        
        WishlistResponse wishlist = wishlistService.updateNotes(user.getId(), productId, notes);
        return ResponseEntity.ok(wishlist);
    }
    
    /**
     * Get wishlist statistics
     * GET /api/wishlist/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<WishlistService.WishlistStatistics> getWishlistStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getUserFromUserDetails(userDetails);
        WishlistService.WishlistStatistics stats = wishlistService.getWishlistStatistics(user.getId());
        return ResponseEntity.ok(stats);
    }
    
    // Helper method
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

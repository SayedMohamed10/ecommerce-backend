package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.model.User;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    // ==================== PUBLIC ENDPOINTS ====================
    
    /**
     * Get reviews for a product
     * GET /api/reviews/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recent") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, currentUserId, page, size, sortBy);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get reviews by rating for a product
     * GET /api/reviews/product/{productId}/rating/{rating}
     */
    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<Page<ReviewResponse>> getProductReviewsByRating(
            @PathVariable Long productId,
            @PathVariable Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        Page<ReviewResponse> reviews = reviewService.getProductReviewsByRating(productId, rating, currentUserId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get verified purchase reviews
     * GET /api/reviews/product/{productId}/verified
     */
    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<Page<ReviewResponse>> getVerifiedPurchaseReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        Page<ReviewResponse> reviews = reviewService.getVerifiedPurchaseReviews(productId, currentUserId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get reviews with images
     * GET /api/reviews/product/{productId}/with-images
     */
    @GetMapping("/product/{productId}/with-images")
    public ResponseEntity<Page<ReviewResponse>> getReviewsWithImages(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        Page<ReviewResponse> reviews = reviewService.getReviewsWithImages(productId, currentUserId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get most helpful reviews
     * GET /api/reviews/product/{productId}/helpful
     */
    @GetMapping("/product/{productId}/helpful")
    public ResponseEntity<List<ReviewResponse>> getMostHelpfulReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        List<ReviewResponse> reviews = reviewService.getMostHelpfulReviews(productId, currentUserId, limit);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Search reviews
     * GET /api/reviews/product/{productId}/search?keyword=great
     */
    @GetMapping("/product/{productId}/search")
    public ResponseEntity<Page<ReviewResponse>> searchReviews(
            @PathVariable Long productId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        Page<ReviewResponse> reviews = reviewService.searchReviews(productId, keyword, currentUserId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Get product review summary
     * GET /api/reviews/product/{productId}/summary
     */
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ProductReviewSummary> getProductReviewSummary(@PathVariable Long productId) {
        ProductReviewSummary summary = reviewService.getProductReviewSummary(productId);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get review by ID
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = userDetails != null ? getUserIdFromUserDetails(userDetails) : null;
        ReviewResponse review = reviewService.getReview(id, currentUserId);
        return ResponseEntity.ok(review);
    }
    
    // ==================== USER ENDPOINTS ====================
    
    /**
     * Create review
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        ReviewResponse review = reviewService.createReview(user.getId(), request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }
    
    /**
     * Update review
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        ReviewResponse review = reviewService.updateReview(user.getId(), id, request);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Delete review
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User user = getUserFromUserDetails(userDetails);
        reviewService.deleteReview(user.getId(), id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Review deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's reviews
     * GET /api/reviews/my-reviews
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<Page<ReviewResponse>> getUserReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getUserFromUserDetails(userDetails);
        Page<ReviewResponse> reviews = reviewService.getUserReviews(user.getId(), page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Mark review as helpful
     * POST /api/reviews/{id}/helpful
     */
    @PostMapping("/{id}/helpful")
    public ResponseEntity<ReviewResponse> markReviewAsHelpful(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User user = getUserFromUserDetails(userDetails);
        ReviewResponse review = reviewService.markReviewAsHelpful(user.getId(), id);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Mark review as not helpful
     * POST /api/reviews/{id}/not-helpful
     */
    @PostMapping("/{id}/not-helpful")
    public ResponseEntity<ReviewResponse> markReviewAsNotHelpful(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User user = getUserFromUserDetails(userDetails);
        ReviewResponse review = reviewService.markReviewAsNotHelpful(user.getId(), id);
        return ResponseEntity.ok(review);
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Get pending reviews (Admin)
     * GET /api/reviews/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewResponse>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ReviewResponse> reviews = reviewService.getPendingReviews(page, size);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Approve review (Admin)
     * PUT /api/reviews/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> approveReview(@PathVariable Long id) {
        ReviewResponse review = reviewService.approveReview(id);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Reject review (Admin)
     * PUT /api/reviews/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> rejectReview(@PathVariable Long id) {
        ReviewResponse review = reviewService.rejectReview(id);
        return ResponseEntity.ok(review);
    }
    
    /**
     * Add admin response (Admin)
     * PUT /api/reviews/{id}/admin-response
     */
    @PutMapping("/{id}/admin-response")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> addAdminResponse(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String response = body.get("response");
        ReviewResponse review = reviewService.addAdminResponse(id, response);
        return ResponseEntity.ok(review);
    }
    
    // Helper methods
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        return user != null ? user.getId() : null;
    }
}

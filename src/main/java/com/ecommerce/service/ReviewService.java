package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    // Create review
    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalStateException("You have already reviewed this product");
        }
        
        // Check if user purchased this product
        boolean hasPurchased = hasUserPurchasedProduct(userId, request.getProductId());
        
        // Create review
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setVerifiedPurchase(hasPurchased);
        review.setImages(request.getImages());
        review.setStatus(Review.ReviewStatus.PENDING); // Pending moderation
        
        review = reviewRepository.save(review);
        
        // Update product average rating and review count
        updateProductRating(request.getProductId());
        
        return mapToResponse(review, userId);
    }
    
    // Update review
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Verify review belongs to user
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        // Update fields
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setImages(request.getImages());
        review.setStatus(Review.ReviewStatus.PENDING); // Reset to pending after edit
        
        review = reviewRepository.save(review);
        
        // Update product average rating
        updateProductRating(review.getProduct().getId());
        
        return mapToResponse(review, userId);
    }
    
    // Delete review
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Verify review belongs to user
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        Long productId = review.getProduct().getId();
        
        reviewRepository.delete(review);
        
        // Update product average rating
        updateProductRating(productId);
    }
    
    // Get review by ID
    public ReviewResponse getReview(Long reviewId, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        return mapToResponse(review, currentUserId);
    }
    
    // Get reviews for a product
    public Page<ReviewResponse> getProductReviews(Long productId, Long currentUserId, int page, int size, String sortBy) {
        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Pageable pageable;
        
        switch (sortBy) {
            case "helpful":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "helpfulCount"));
                break;
            case "recent":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case "rating_high":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
                break;
            case "rating_low":
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rating"));
                break;
            default:
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        
        Page<Review> reviews = reviewRepository.findByProductIdAndStatus(
                productId, Review.ReviewStatus.APPROVED, pageable);
        
        return reviews.map(review -> mapToResponse(review, currentUserId));
    }
    
    // Get reviews by rating
    public Page<ReviewResponse> getProductReviewsByRating(Long productId, Integer rating, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByProductIdAndStatusAndRating(
                productId, Review.ReviewStatus.APPROVED, rating, pageable);
        
        return reviews.map(review -> mapToResponse(review, currentUserId));
    }
    
    // Get verified purchase reviews
    public Page<ReviewResponse> getVerifiedPurchaseReviews(Long productId, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByProductIdAndStatusAndVerifiedPurchase(
                productId, Review.ReviewStatus.APPROVED, true, pageable);
        
        return reviews.map(review -> mapToResponse(review, currentUserId));
    }
    
    // Get reviews with images
    public Page<ReviewResponse> getReviewsWithImages(Long productId, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByProductIdWithImages(productId, pageable);
        
        return reviews.map(review -> mapToResponse(review, currentUserId));
    }
    
    // Get most helpful reviews
    public List<ReviewResponse> getMostHelpfulReviews(Long productId, Long currentUserId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findMostHelpfulByProductId(productId, pageable);
        
        return reviews.stream()
                .map(review -> mapToResponse(review, currentUserId))
                .collect(Collectors.toList());
    }
    
    // Search reviews
    public Page<ReviewResponse> searchReviews(Long productId, String keyword, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.searchReviewsByProductId(productId, keyword, pageable);
        
        return reviews.map(review -> mapToResponse(review, currentUserId));
    }
    
    // Get user's reviews
    public Page<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        
        return reviews.map(review -> mapToResponse(review, userId));
    }
    
    // Get product review summary
    public ProductReviewSummary getProductReviewSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        ProductReviewSummary summary = new ProductReviewSummary();
        summary.setProductId(productId);
        summary.setProductName(product.getName());
        
        // Total reviews
        Long totalReviews = reviewRepository.countByProductIdAndStatus(productId, Review.ReviewStatus.APPROVED);
        summary.setTotalReviews(totalReviews);
        
        // Average rating
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        summary.setAverageRating(averageRating != null ? averageRating : 0.0);
        
        // Rating distribution
        List<Object[]> distribution = reviewRepository.getRatingDistributionByProductId(productId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (Object[] row : distribution) {
            ratingDistribution.put((Integer) row[0], (Long) row[1]);
        }
        summary.setRatingDistribution(ratingDistribution);
        
        // Calculate percentages
        summary.calculatePercentages();
        
        // Verified purchase count
        Long verifiedCount = totalReviews; // Simplified - can add specific query
        summary.setVerifiedPurchaseCount(verifiedCount);
        
        // Reviews with images
        summary.setReviewsWithImages(0L); // Simplified - can add specific query
        
        return summary;
    }
    
    // Mark review as helpful
    @Transactional
    public ReviewResponse markReviewAsHelpful(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.incrementHelpfulCount();
        review = reviewRepository.save(review);
        
        return mapToResponse(review, userId);
    }
    
    // Mark review as not helpful
    @Transactional
    public ReviewResponse markReviewAsNotHelpful(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.incrementNotHelpfulCount();
        review = reviewRepository.save(review);
        
        return mapToResponse(review, userId);
    }
    
    // Admin: Approve review
    @Transactional
    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setStatus(Review.ReviewStatus.APPROVED);
        review = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(review.getProduct().getId());
        
        return mapToResponse(review, null);
    }
    
    // Admin: Reject review
    @Transactional
    public ReviewResponse rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setStatus(Review.ReviewStatus.REJECTED);
        review = reviewRepository.save(review);
        
        // Update product rating
        updateProductRating(review.getProduct().getId());
        
        return mapToResponse(review, null);
    }
    
    // Admin: Add admin response
    @Transactional
    public ReviewResponse addAdminResponse(Long reviewId, String response) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setAdminResponse(response);
        review.setAdminResponseAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        
        return mapToResponse(review, null);
    }
    
    // Admin: Get pending reviews
    public Page<ReviewResponse> getPendingReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByStatus(Review.ReviewStatus.PENDING, pageable);
        
        return reviews.map(review -> mapToResponse(review, null));
    }
    
    // Helper: Check if user purchased product
    private boolean hasUserPurchasedProduct(Long userId, Long productId) {
        List<OrderItem> orderItems = orderItemRepository.findByProductId(productId);
        return orderItems.stream()
                .anyMatch(item -> item.getOrder().getUser().getId().equals(userId) 
                        && item.getOrder().getPaymentStatus() == Order.PaymentStatus.PAID);
    }
    
    // Helper: Update product average rating and review count
    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId);
        Long reviewCount = reviewRepository.countByProductIdAndStatus(productId, Review.ReviewStatus.APPROVED);
        
        product.setAverageRating(averageRating != null ? averageRating : 0.0);
        product.setReviewCount(reviewCount.intValue());
        
        productRepository.save(product);
    }
    
    // Helper: Map Review to ReviewResponse
    private ReviewResponse mapToResponse(Review review, Long currentUserId) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUser().getId());
        response.setUserName(review.getUser().getName());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getName());
        response.setRating(review.getRating());
        response.setTitle(review.getTitle());
        response.setComment(review.getComment());
        response.setVerifiedPurchase(review.getVerifiedPurchase());
        response.setImages(review.getImages());
        response.setHelpfulCount(review.getHelpfulCount());
        response.setNotHelpfulCount(review.getNotHelpfulCount());
        response.setStatus(review.getStatus().name());
        response.setAdminResponse(review.getAdminResponse());
        response.setAdminResponseAt(review.getAdminResponseAt());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        
        return response;
    }
}

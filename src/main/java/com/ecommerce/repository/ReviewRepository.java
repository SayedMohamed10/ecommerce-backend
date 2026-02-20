package com.ecommerce.repository;

import com.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find all approved reviews for a product
    Page<Review> findByProductIdAndStatus(Long productId, Review.ReviewStatus status, Pageable pageable);
    
    // Find all reviews by user
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    // Find review by user and product
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
    
    // Check if user has reviewed product
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Find all reviews by rating
    Page<Review> findByProductIdAndStatusAndRating(Long productId, Review.ReviewStatus status, Integer rating, Pageable pageable);
    
    // Find reviews pending moderation
    Page<Review> findByStatus(Review.ReviewStatus status, Pageable pageable);
    
    // Count reviews by product
    Long countByProductIdAndStatus(Long productId, Review.ReviewStatus status);
    
    // Get average rating for product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // Get rating distribution for product
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);
    
    // Find verified purchase reviews
    Page<Review> findByProductIdAndStatusAndVerifiedPurchase(Long productId, Review.ReviewStatus status, Boolean verifiedPurchase, Pageable pageable);
    
    // Find most helpful reviews
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED' ORDER BY r.helpfulCount DESC")
    List<Review> findMostHelpfulByProductId(@Param("productId") Long productId, Pageable pageable);
    
    // Find reviews with images
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED' AND SIZE(r.images) > 0")
    Page<Review> findByProductIdWithImages(@Param("productId") Long productId, Pageable pageable);
    
    // Search reviews by keyword
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.status = 'APPROVED' " +
           "AND (LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Review> searchReviewsByProductId(@Param("productId") Long productId, @Param("keyword") String keyword, Pageable pageable);
}

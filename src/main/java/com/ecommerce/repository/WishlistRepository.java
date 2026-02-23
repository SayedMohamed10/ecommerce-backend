package com.ecommerce.repository;

import com.ecommerce.model.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    // Find all wishlist items for a user
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);
    
    // Find all wishlist items for a user (no pagination)
    List<Wishlist> findByUserId(Long userId);
    
    // Find wishlist item by user and product
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);
    
    // Check if product is in user's wishlist
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Delete wishlist item by user and product
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    // Delete all wishlist items for a user
    void deleteByUserId(Long userId);
    
    // Count wishlist items for a user
    Long countByUserId(Long userId);
    
    // Find wishlist items with product details (eager loading)
    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.product WHERE w.user.id = :userId")
    List<Wishlist> findByUserIdWithProduct(@Param("userId") Long userId);
    
    // Find users who wishlisted a product
    List<Wishlist> findByProductId(Long productId);
    
    // Count users who wishlisted a product
    Long countByProductId(Long productId);
}

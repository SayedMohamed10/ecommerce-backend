package com.ecommerce.repository;

import com.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find all cart items for a user
    List<CartItem> findByUserId(Long userId);
    
    // Find specific cart item for user and product
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    
    // Check if item exists in cart
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Delete all cart items for a user (clear cart)
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    // Count items in user's cart
    Long countByUserId(Long userId);
    
    // Get cart items with product details (eager loading)
    @Query("SELECT c FROM CartItem c " +
           "JOIN FETCH c.product p " +
           "WHERE c.user.id = :userId " +
           "ORDER BY c.addedAt DESC")
    List<CartItem> findByUserIdWithProduct(@Param("userId") Long userId);
    
    // Find cart items for a specific product (useful for stock management)
    List<CartItem> findByProductId(Long productId);
    
    // Delete specific cart item
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId AND c.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}

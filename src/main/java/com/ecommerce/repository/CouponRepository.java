package com.ecommerce.repository;

import com.ecommerce.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    // Find coupon by code
    Optional<Coupon> findByCode(String code);
    
    // Find coupon by code (case insensitive)
    Optional<Coupon> findByCodeIgnoreCase(String code);
    
    // Check if code exists
    boolean existsByCode(String code);
    
    // Find active coupons
    Page<Coupon> findByActiveTrue(Pageable pageable);
    
    // Find coupons by discount type
    List<Coupon> findByDiscountType(Coupon.DiscountType discountType);
    
    // Find valid coupons (active, not expired, not started, not reached limit)
    @Query("SELECT c FROM Coupon c WHERE c.active = true " +
           "AND (c.startsAt IS NULL OR c.startsAt <= :now) " +
           "AND (c.expiresAt IS NULL OR c.expiresAt > :now) " +
           "AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit)")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);
    
    // Find coupons expiring soon
    @Query("SELECT c FROM Coupon c WHERE c.active = true " +
           "AND c.expiresAt BETWEEN :now AND :endDate")
    List<Coupon> findCouponsExpiringSoon(@Param("now") LocalDateTime now, @Param("endDate") LocalDateTime endDate);
    
    // Find coupons by category
    List<Coupon> findByCategoryId(Long categoryId);
    
    // Find coupons by product
    List<Coupon> findByProductId(Long productId);
    
    // Find coupons for first purchase
    List<Coupon> findByFirstPurchaseOnlyTrue();
    
    // Count active coupons
    Long countByActiveTrue();
}

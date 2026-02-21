package com.ecommerce.repository;

import com.ecommerce.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payment by Stripe payment intent ID
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    // Find payments by order
    List<Payment> findByOrderId(Long orderId);
    
    // Find payments by user
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    
    // Find payments by status
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);
    
    // Find successful payments by user
    Page<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status, Pageable pageable);
    
    // Count payments by user
    Long countByUserId(Long userId);
}

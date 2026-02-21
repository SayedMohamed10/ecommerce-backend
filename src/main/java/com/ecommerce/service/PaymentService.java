package com.ecommerce.service;

import com.ecommerce.dto.CreatePaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    
    @Value("${stripe.api.key:sk_test_dummy}")
    private String stripeApiKey;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create payment intent (Step 1 of payment process)
    @Transactional
    public PaymentResponse createPaymentIntent(Long userId, CreatePaymentRequest request) {
        // Get order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        // Check if order is already paid
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new IllegalStateException("Order is already paid");
        }
        
        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(order.getUser());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        
        // In production, call Stripe API here:
        // Stripe.apiKey = stripeApiKey;
        // PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        //     .setAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue())
        //     .setCurrency(request.getCurrency())
        //     .build();
        // PaymentIntent intent = PaymentIntent.create(params);
        // payment.setStripePaymentIntentId(intent.getId());
        
        // For now, generate dummy payment intent ID
        String dummyPaymentIntentId = "pi_" + System.currentTimeMillis();
        payment.setStripePaymentIntentId(dummyPaymentIntentId);
        
        payment = paymentRepository.save(payment);
        
        PaymentResponse response = mapToResponse(payment);
        response.setClientSecret(dummyPaymentIntentId + "_secret"); // Dummy client secret
        response.setPaymentIntentId(dummyPaymentIntentId);
        
        return response;
    }
    
    // Confirm payment (Step 2 - called after Stripe confirms)
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId, Map<String, String> paymentDetails) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        // Update payment status
        payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setPaymentMethod(Payment.PaymentMethod.CARD);
        
        // Save card details (last 4 digits, brand)
        if (paymentDetails != null) {
            payment.setCardLast4(paymentDetails.get("cardLast4"));
            payment.setCardBrand(paymentDetails.get("cardBrand"));
            payment.setReceiptUrl(paymentDetails.get("receiptUrl"));
        }
        
        payment = paymentRepository.save(payment);
        
        // Update order payment status
        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setPaymentTransactionId(paymentIntentId);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
        
        return mapToResponse(payment);
    }
    
    // Handle payment failure
    @Transactional
    public PaymentResponse handlePaymentFailure(String paymentIntentId, String failureMessage) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailureMessage(failureMessage);
        payment.setCompletedAt(LocalDateTime.now());
        
        payment = paymentRepository.save(payment);
        
        // Update order
        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.FAILED);
        orderRepository.save(order);
        
        return mapToResponse(payment);
    }
    
    // Refund payment
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        // Verify payment was successful
        if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }
        
        // Verify refund amount
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalStateException("Refund amount cannot exceed payment amount");
        }
        
        // In production, call Stripe refund API here:
        // Refund refund = Refund.create(RefundCreateParams.builder()
        //     .setPaymentIntent(payment.getStripePaymentIntentId())
        //     .setAmount(refundAmount.multiply(BigDecimal.valueOf(100)).longValue())
        //     .build());
        
        payment.setRefunded(true);
        payment.setRefundAmount(refundAmount);
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        
        payment = paymentRepository.save(payment);
        
        // Update order
        Order order = payment.getOrder();
        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        order.setStatus(Order.OrderStatus.REFUNDED);
        orderRepository.save(order);
        
        return mapToResponse(payment);
    }
    
    // Get payment by ID
    public PaymentResponse getPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        // Verify payment belongs to user
        if (!payment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        return mapToResponse(payment);
    }
    
    // Get user's payment history
    public Page<PaymentResponse> getUserPaymentHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(this::mapToResponse);
    }
    
    // Get successful payments for user
    public Page<PaymentResponse> getUserSuccessfulPayments(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> payments = paymentRepository.findByUserIdAndStatus(
                userId, Payment.PaymentStatus.SUCCEEDED, pageable);
        return payments.map(this::mapToResponse);
    }
    
    // Map Payment to PaymentResponse
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setOrderNumber(payment.getOrder().getOrderNumber());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus().name());
        
        if (payment.getPaymentMethod() != null) {
            response.setPaymentMethod(payment.getPaymentMethod().name());
        }
        
        response.setCardLast4(payment.getCardLast4());
        response.setCardBrand(payment.getCardBrand());
        response.setReceiptUrl(payment.getReceiptUrl());
        response.setFailureMessage(payment.getFailureMessage());
        response.setRefunded(payment.getRefunded());
        response.setRefundAmount(payment.getRefundAmount());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setPaymentIntentId(payment.getStripePaymentIntentId());
        
        return response;
    }
}

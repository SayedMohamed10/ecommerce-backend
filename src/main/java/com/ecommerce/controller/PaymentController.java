package com.ecommerce.controller;

import com.ecommerce.dto.CreatePaymentRequest;
import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.model.User;
import com.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private com.ecommerce.repository.UserRepository userRepository;
    
    /**
     * Create payment intent
     * POST /api/payments/create-intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePaymentRequest request) {
        
        User user = getUserFromUserDetails(userDetails);
        PaymentResponse payment = paymentService.createPaymentIntent(user.getId(), request);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }
    
    /**
     * Confirm payment (webhook or manual confirmation)
     * POST /api/payments/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody Map<String, String> body) {
        String paymentIntentId = body.get("paymentIntentId");
        
        Map<String, String> paymentDetails = new HashMap<>();
        paymentDetails.put("cardLast4", body.get("cardLast4"));
        paymentDetails.put("cardBrand", body.get("cardBrand"));
        paymentDetails.put("receiptUrl", body.get("receiptUrl"));
        
        PaymentResponse payment = paymentService.confirmPayment(paymentIntentId, paymentDetails);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Handle payment failure
     * POST /api/payments/failure
     */
    @PostMapping("/failure")
    public ResponseEntity<PaymentResponse> handlePaymentFailure(@RequestBody Map<String, String> body) {
        String paymentIntentId = body.get("paymentIntentId");
        String failureMessage = body.get("failureMessage");
        
        PaymentResponse payment = paymentService.handlePaymentFailure(paymentIntentId, failureMessage);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Get payment by ID
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        User user = getUserFromUserDetails(userDetails);
        PaymentResponse payment = paymentService.getPayment(user.getId(), id);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Get payment history
     * GET /api/payments/history
     */
    @GetMapping("/history")
    public ResponseEntity<Page<PaymentResponse>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getUserFromUserDetails(userDetails);
        Page<PaymentResponse> payments = paymentService.getUserPaymentHistory(user.getId(), page, size);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get successful payments
     * GET /api/payments/successful
     */
    @GetMapping("/successful")
    public ResponseEntity<Page<PaymentResponse>> getSuccessfulPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getUserFromUserDetails(userDetails);
        Page<PaymentResponse> payments = paymentService.getUserSuccessfulPayments(user.getId(), page, size);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Refund payment (Admin)
     * POST /api/payments/{id}/refund
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        BigDecimal refundAmount = new BigDecimal(body.get("refundAmount"));
        PaymentResponse payment = paymentService.refundPayment(id, refundAmount);
        return ResponseEntity.ok(payment);
    }
    
    // Helper method
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Get user's cart
    public CartResponse getCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        return buildCartResponse(cartItems);
    }
    
    // Add item to cart
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify product exists and is active
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!product.getActive()) {
            throw new IllegalStateException("Product is not available");
        }
        
        // Check stock availability
        if (product.getStock() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock. Available: " + product.getStock());
        }
        
        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);
        
        if (cartItem != null) {
            // Update quantity if item exists
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            if (product.getStock() < newQuantity) {
                throw new IllegalStateException("Cannot add more. Maximum available: " + product.getStock());
            }
            
            cartItem.setQuantity(newQuantity);
            cartItem.setPriceAtAddition(product.getEffectivePrice());
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtAddition(product.getEffectivePrice());
        }
        
        cartItemRepository.save(cartItem);
        
        // Return updated cart
        return getCart(userId);
    }
    
    // Update cart item quantity
    @Transactional
    public CartResponse updateCartItem(Long userId, Long productId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        Product product = cartItem.getProduct();
        
        // Check stock availability
        if (product.getStock() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock. Available: " + product.getStock());
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItem.setPriceAtAddition(product.getEffectivePrice());
        cartItemRepository.save(cartItem);
        
        return getCart(userId);
    }
    
    // Remove item from cart
    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        if (!cartItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        
        return getCart(userId);
    }
    
    // Clear entire cart
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
    
    // Get cart item count
    public Long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }
    
    // Validate cart before checkout
    public CartValidationResponse validateCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        
        CartValidationResponse response = new CartValidationResponse();
        response.setValid(true);
        
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            
            // Check if product is active
            if (!product.getActive()) {
                response.setValid(false);
                response.addError("Product '" + product.getName() + "' is no longer available");
                continue;
            }
            
            // Check stock
            if (product.getStock() < item.getQuantity()) {
                response.setValid(false);
                response.addError("Insufficient stock for '" + product.getName() + 
                                "'. Available: " + product.getStock() + 
                                ", Requested: " + item.getQuantity());
            }
            
            // Check price changes
            BigDecimal currentPrice = product.getEffectivePrice();
            if (currentPrice.compareTo(item.getPriceAtAddition()) != 0) {
                response.addWarning("Price changed for '" + product.getName() + 
                                  "'. Old: $" + item.getPriceAtAddition() + 
                                  ", New: $" + currentPrice);
            }
        }
        
        return response;
    }
    
    // Helper method to build cart response
    private CartResponse buildCartResponse(List<CartItem> cartItems) {
        CartResponse response = new CartResponse();
        
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
        
        response.setItems(itemResponses);
        response.setSummary(calculateSummary(cartItems));
        
        // Add messages for unavailable items
        for (CartItem item : cartItems) {
            if (item.hasStockIssue()) {
                response.addMessage("'" + item.getProduct().getName() + "' has limited stock or is unavailable");
            }
        }
        
        return response;
    }
    
    // Helper method to map CartItem to CartItemResponse
    private CartItemResponse mapToItemResponse(CartItem item) {
        Product product = item.getProduct();
        
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setProductSlug(product.getSlug());
        response.setProductImage(product.getMainImage());
        response.setPrice(product.getPrice());
        response.setDiscountPrice(product.getDiscountPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getSubtotal());
        response.setAvailableStock(product.getStock());
        response.setInStock(product.isInStock());
        response.setProductActive(product.getActive());
        response.setAddedAt(item.getAddedAt());
        response.setAvailable(item.isAvailable());
        
        // Set availability message
        if (!product.getActive()) {
            response.setAvailabilityMessage("Product is no longer available");
        } else if (product.getStock() < item.getQuantity()) {
            response.setAvailabilityMessage("Only " + product.getStock() + " available");
        } else {
            response.setAvailabilityMessage("In stock");
        }
        
        return response;
    }
    
    // Helper method to calculate cart summary
    private CartResponse.CartSummary calculateSummary(List<CartItem> cartItems) {
        CartResponse.CartSummary summary = new CartResponse.CartSummary();
        
        int totalItems = cartItems.size();
        int totalQuantity = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discount = BigDecimal.ZERO;
        // Calculate discount if products have discount prices
        for (CartItem item : cartItems) {
            if (item.getProduct().hasDiscount()) {
                BigDecimal regularTotal = item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal discountedTotal = item.getSubtotal();
                discount = discount.add(regularTotal.subtract(discountedTotal));
            }
        }
        
        // For now, tax and shipping are 0 (can be calculated based on location later)
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal shipping = BigDecimal.ZERO;
        
        BigDecimal total = subtotal.add(tax).add(shipping);
        
        boolean hasUnavailableItems = cartItems.stream()
                .anyMatch(CartItem::hasStockIssue);
        
        summary.setTotalItems(totalItems);
        summary.setTotalQuantity(totalQuantity);
        summary.setSubtotal(subtotal);
        summary.setDiscount(discount);
        summary.setTax(tax);
        summary.setShipping(shipping);
        summary.setTotal(total);
        summary.setHasUnavailableItems(hasUnavailableItems);
        
        return summary;
    }
    
    // Inner class for cart validation response
    @lombok.Data
    public static class CartValidationResponse {
        private Boolean valid;
        private List<String> errors = new java.util.ArrayList<>();
        private List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
    }
}

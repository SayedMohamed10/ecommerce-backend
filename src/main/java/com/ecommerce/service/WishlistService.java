package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.AddToWishlistRequest;
import com.ecommerce.dto.WishlistResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartService cartService;
    
    // Add to wishlist
    @Transactional
    public WishlistResponse addToWishlist(Long userId, AddToWishlistRequest request) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new IllegalStateException("Product is already in your wishlist");
        }
        
        // Create wishlist item
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setNotes(request.getNotes());
        
        wishlist = wishlistRepository.save(wishlist);
        
        return mapToResponse(wishlist);
    }
    
    // Remove from wishlist
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        // Check if exists
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Product not found in wishlist");
        }
        
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    // Get user's wishlist
    public List<WishlistResponse> getWishlist(Long userId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUserIdWithProduct(userId);
        
        return wishlistItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Get wishlist with pagination
    public Page<WishlistResponse> getWishlistPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "addedAt"));
        Page<Wishlist> wishlistItems = wishlistRepository.findByUserId(userId, pageable);
        
        return wishlistItems.map(this::mapToResponse);
    }
    
    // Get wishlist count
    public Long getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }
    
    // Check if product is in wishlist
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    // Move to cart
    @Transactional
    public void moveToCart(Long userId, Long productId) {
        // Verify product is in wishlist
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));
        
        // Add to cart (default quantity = 1)
        AddToCartRequest cartRequest = new AddToCartRequest();
        cartRequest.setProductId(productId);
        cartRequest.setQuantity(1);
        
        cartService.addToCart(userId, cartRequest);
        
        // Remove from wishlist
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
    
    // Move all to cart
    @Transactional
    public void moveAllToCart(Long userId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUserId(userId);
        
        for (Wishlist item : wishlistItems) {
            // Only add if product is in stock
            if (item.getProduct().getActive() && item.getProduct().getStock() > 0) {
                AddToCartRequest cartRequest = new AddToCartRequest();
                cartRequest.setProductId(item.getProduct().getId());
                cartRequest.setQuantity(1);
                
                try {
                    cartService.addToCart(userId, cartRequest);
                } catch (Exception e) {
                    // Skip if error (e.g., already in cart)
                    continue;
                }
            }
        }
        
        // Clear wishlist
        wishlistRepository.deleteByUserId(userId);
    }
    
    // Clear wishlist
    @Transactional
    public void clearWishlist(Long userId) {
        wishlistRepository.deleteByUserId(userId);
    }
    
    // Update notes
    @Transactional
    public WishlistResponse updateNotes(Long userId, Long productId, String notes) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));
        
        wishlist.setNotes(notes);
        wishlist = wishlistRepository.save(wishlist);
        
        return mapToResponse(wishlist);
    }
    
    // Get wishlist statistics
    public WishlistStatistics getWishlistStatistics(Long userId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUserIdWithProduct(userId);
        
        long totalItems = wishlistItems.size();
        long inStockItems = wishlistItems.stream()
                .filter(w -> w.getProduct().getActive() && w.getProduct().getStock() > 0)
                .count();
        long outOfStockItems = totalItems - inStockItems;
        
        BigDecimal totalValue = wishlistItems.stream()
                .map(w -> w.getProduct().hasDiscount() ? 
                    w.getProduct().getDiscountPrice() : w.getProduct().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new WishlistStatistics(totalItems, inStockItems, outOfStockItems, totalValue);
    }
    
    // Map Wishlist to WishlistResponse
    private WishlistResponse mapToResponse(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        
        WishlistResponse response = new WishlistResponse();
        response.setId(wishlist.getId());
        response.setUserId(wishlist.getUser().getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setProductSku(product.getSku());
        response.setProductDescription(product.getDescription());
        response.setProductPrice(product.getPrice());
        response.setProductDiscountPrice(product.getDiscountPrice());
        response.setProductMainImage(product.getMainImage());
        response.setProductInStock(product.getStock() > 0);
        response.setProductStock(product.getStock());
        response.setProductActive(product.getActive());
        response.setNotes(wishlist.getNotes());
        response.setAddedAt(wishlist.getAddedAt());
        
        // Calculated fields
        response.setHasDiscount(product.hasDiscount());
        if (product.hasDiscount()) {
            BigDecimal discountAmount = product.getPrice().subtract(product.getDiscountPrice());
            BigDecimal discountPercentage = discountAmount
                    .divide(product.getPrice(), 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setDiscountPercentage(discountPercentage);
        }
        response.setIsAvailable(product.getActive() && product.getStock() > 0);
        
        return response;
    }
    
    // Inner class for wishlist statistics
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class WishlistStatistics {
        private Long totalItems;
        private Long inStockItems;
        private Long outOfStockItems;
        private BigDecimal totalValue;
    }
}

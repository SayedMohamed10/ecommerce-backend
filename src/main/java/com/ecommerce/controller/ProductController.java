package com.ecommerce.controller;

import com.ecommerce.dto.ProductListResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // ==================== PUBLIC ENDPOINTS ====================
    
    /**
     * Get all products with pagination and sorting
     * GET /api/products?page=0&size=12&sortBy=createdAt&sortDirection=desc
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ProductListResponse response = productService.getAllProducts(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search products
     * GET /api/products/search?keyword=laptop&page=0&size=12
     */
    @GetMapping("/search")
    public ResponseEntity<ProductListResponse> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ProductListResponse response = productService.searchProducts(keyword, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Advanced search with filters
     * GET /api/products/advanced-search?keyword=phone&categoryId=1&minPrice=100&maxPrice=1000&brand=Apple
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<ProductListResponse> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ProductListResponse response = productService.advancedSearch(
            keyword, categoryId, minPrice, maxPrice, brand, minRating,
            page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product by ID
     * GET /api/products/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product by slug
     * GET /api/products/slug/macbook-pro-2024
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        ProductResponse response = productService.getProductBySlug(slug);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get products by category
     * GET /api/products/category/1?page=0&size=12
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ProductListResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ProductListResponse response = productService.getProductsByCategory(
            categoryId, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get products by category with filters
     * GET /api/products/category/1/filter?minPrice=100&maxPrice=1000
     */
    @GetMapping("/category/{categoryId}/filter")
    public ResponseEntity<ProductListResponse> getProductsByCategoryWithFilters(
            @PathVariable Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ProductListResponse response = productService.getProductsByCategoryWithFilters(
            categoryId, keyword, minPrice, maxPrice, brand, minRating,
            page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get featured products
     * GET /api/products/featured?page=0&size=8
     */
    @GetMapping("/featured")
    public ResponseEntity<ProductListResponse> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        ProductListResponse response = productService.getFeaturedProducts(page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get new arrivals
     * GET /api/products/new-arrivals?page=0&size=8
     */
    @GetMapping("/new-arrivals")
    public ResponseEntity<ProductListResponse> getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        ProductListResponse response = productService.getNewArrivals(page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get best sellers
     * GET /api/products/best-sellers?page=0&size=8
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<ProductListResponse> getBestSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        ProductListResponse response = productService.getBestSellers(page, size);
        return ResponseEntity.ok(response);
    }
    
    // Continues in Part 2...
// Part 2 - Add these methods to ProductController from Part 1

    /**
     * Get popular products
     * GET /api/products/popular?page=0&size=8
     */
    @GetMapping("/popular")
    public ResponseEntity<ProductListResponse> getPopularProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        
        ProductListResponse response = productService.getPopularProducts(page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get discounted products
     * GET /api/products/discounted?page=0&size=12
     */
    @GetMapping("/discounted")
    public ResponseEntity<ProductListResponse> getDiscountedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        ProductListResponse response = productService.getDiscountedProducts(page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get related products
     * GET /api/products/1/related?limit=4
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductResponse>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "4") int limit) {
        
        List<ProductResponse> response = productService.getRelatedProducts(id, limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all brands
     * GET /api/products/filters/brands
     */
    @GetMapping("/filters/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }
    
    /**
     * Get price range
     * GET /api/products/filters/price-range
     */
    @GetMapping("/filters/price-range")
    public ResponseEntity<ProductService.PriceRange> getPriceRange() {
        ProductService.PriceRange priceRange = productService.getPriceRange();
        return ResponseEntity.ok(priceRange);
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Create new product (Admin only)
     * POST /api/products
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Update product (Admin only)
     * PUT /api/products/1
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete product (Admin only)
     * DELETE /api/products/1
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update product stock (Admin only)
     * PATCH /api/products/1/stock
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        
        productService.updateStock(id, quantity);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Stock updated successfully");
        
        return ResponseEntity.ok(response);
    }
}

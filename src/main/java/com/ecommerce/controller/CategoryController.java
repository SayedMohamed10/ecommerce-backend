package com.ecommerce.controller;

import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    // ==================== PUBLIC ENDPOINTS ====================
    
    /**
     * Get all categories
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get all active categories
     * GET /api/categories/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get parent categories only
     * GET /api/categories/parents
     */
    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponse>> getParentCategories() {
        List<CategoryResponse> categories = categoryService.getParentCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get category by ID
     * GET /api/categories/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Get category by ID with products
     * GET /api/categories/1/with-products
     */
    @GetMapping("/{id}/with-products")
    public ResponseEntity<CategoryResponse> getCategoryByIdWithProducts(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryByIdWithProducts(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Get subcategories of a parent category
     * GET /api/categories/1/subcategories
     */
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(@PathVariable Long parentId) {
        List<CategoryResponse> subcategories = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(subcategories);
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Create new category (Admin only)
     * POST /api/categories
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }
    
    /**
     * Update category (Admin only)
     * PUT /api/categories/1
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Delete category (Admin only)
     * DELETE /api/categories/1
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        
        return ResponseEntity.ok(response);
    }
}

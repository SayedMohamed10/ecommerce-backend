package com.ecommerce.service;

import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.exception.EmailAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllOrdered()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllActiveOrdered()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CategoryResponse> getParentCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }
    
    public CategoryResponse getCategoryByIdWithProducts(Long id) {
        Category category = categoryRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }
    
    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdAndActiveTrue(parentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new EmailAlreadyExistsException("Category with name '" + request.getName() + "' already exists");
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setActive(request.getActive());
        category.setDisplayOrder(request.getDisplayOrder());
        
        // Set parent category if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }
    
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if new name conflicts with existing category
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new EmailAlreadyExistsException("Category with name '" + request.getName() + "' already exists");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setActive(request.getActive());
        category.setDisplayOrder(request.getDisplayOrder());
        
        // Update parent category if provided
        if (request.getParentId() != null) {
            // Prevent setting itself as parent
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        Category updatedCategory = categoryRepository.save(category);
        return mapToResponse(updatedCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if category has products
        if (category.getProductCount() > 0) {
            throw new IllegalStateException("Cannot delete category with products. Please reassign or delete products first.");
        }
        
        // Check if category has subcategories
        if (category.hasChildren()) {
            throw new IllegalStateException("Cannot delete category with subcategories. Please delete or reassign subcategories first.");
        }
        
        categoryRepository.delete(category);
    }
    
    // Helper method to map entity to response
    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());
        response.setActive(category.getActive());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setProductCount(category.getProductCount());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // Set parent
        if (category.getParent() != null) {
            CategoryResponse.ParentCategory parent = new CategoryResponse.ParentCategory();
            parent.setId(category.getParent().getId());
            parent.setName(category.getParent().getName());
            response.setParent(parent);
        }
        
        // Set children
        if (category.hasChildren()) {
            List<CategoryResponse.SubCategory> children = category.getChildren().stream()
                    .map(child -> {
                        CategoryResponse.SubCategory subCat = new CategoryResponse.SubCategory();
                        subCat.setId(child.getId());
                        subCat.setName(child.getName());
                        subCat.setImageUrl(child.getImageUrl());
                        subCat.setProductCount(child.getProductCount());
                        return subCat;
                    })
                    .collect(Collectors.toList());
            response.setChildren(children);
        }
        
        return response;
    }
}

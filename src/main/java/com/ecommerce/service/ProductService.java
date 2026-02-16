package com.ecommerce.service;

import com.ecommerce.dto.ProductListResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Get all products with pagination
    public ProductListResponse getAllProducts(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                                  Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        
        return buildProductListResponse(productPage, null, null, null, null, null, null, sortBy, sortDirection);
    }
    
    // Get product by ID
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Increment view count
        product.incrementViewCount();
        productRepository.save(product);
        
        return mapToResponse(product);
    }
    
    // Get product by slug
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        
        // Increment view count
        product.incrementViewCount();
        productRepository.save(product);
        
        return mapToResponse(product);
    }
    
    // Search products
    public ProductListResponse searchProducts(String keyword, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                                  Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);
        
        return buildProductListResponse(productPage, keyword, null, null, null, null, null, sortBy, sortDirection);
    }
    
    // Get products by category
    public ProductListResponse getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDirection) {
        // Verify category exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                                  Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        
        return buildProductListResponse(productPage, null, categoryId, category.getName(), null, null, null, sortBy, sortDirection);
    }
    
    // Get products by category with filters
    public ProductListResponse getProductsByCategoryWithFilters(
            Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            String brand, Double minRating, int page, int size, String sortBy, String sortDirection) {
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                                  Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Specification<Product> spec = buildSpecification(categoryId, keyword, minPrice, maxPrice, brand, minRating);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        return buildProductListResponse(productPage, keyword, categoryId, category.getName(), 
                                        minPrice, maxPrice, brand, sortBy, sortDirection);
    }
    
    // Advanced search with all filters
    public ProductListResponse advancedSearch(
            String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String brand, Double minRating, int page, int size, String sortBy, String sortDirection) {
        
        String categoryName = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            categoryName = category.getName();
        }
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                                  Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Specification<Product> spec = buildSpecification(categoryId, keyword, minPrice, maxPrice, brand, minRating);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        return buildProductListResponse(productPage, keyword, categoryId, categoryName, 
                                        minPrice, maxPrice, brand, sortBy, sortDirection);
    }
    
    // Get featured products
    public ProductListResponse getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage = productRepository.findByActiveTrueAndFeaturedTrue(pageable);
        
        return buildProductListResponse(productPage, null, null, null, null, null, null, "createdAt", "desc");
    }
    
    // Get new arrivals
    public ProductListResponse getNewArrivals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findNewArrivals(pageable);
        
        return buildProductListResponse(productPage, null, null, null, null, null, null, "createdAt", "desc");
    }
    
    // Get best sellers
    public ProductListResponse getBestSellers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findBestSellers(pageable);
        
        return buildProductListResponse(productPage, null, null, null, null, null, null, "soldCount", "desc");
    }
    
    // Get popular products
    public ProductListResponse getPopularProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findPopularProducts(pageable);
        
        return buildProductListResponse(productPage, null, null, null, null, null, null, "viewCount", "desc");
    }
    
    // Get discounted products
    public ProductListResponse getDiscountedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "discountPrice"));
        Page<Product> productPage = productRepository.findDiscountedProducts(pageable);
        
        return buildProductListResponse(productPage, null, null, null, null, null, null, "discountPrice", "desc");
    }
    
    // Get related products
    public List<ProductResponse> getRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<Product> relatedProducts = productRepository.findRelatedProducts(
                product.getCategory().getId(), productId, pageable);
        
        return relatedProducts.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Get all brands
    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }
    
    // Get price range
    public PriceRange getPriceRange() {
        BigDecimal minPrice = productRepository.findMinPrice();
        BigDecimal maxPrice = productRepository.findMaxPrice();
        return new PriceRange(
            minPrice != null ? minPrice : BigDecimal.ZERO,
            maxPrice != null ? maxPrice : BigDecimal.valueOf(10000)
        );
    }
    
    // Continues in next part...
package com.ecommerce.service;

// This is Part 2 - Add these methods to the ProductService class from Part 1

    // Create product
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Verify category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Check if SKU already exists
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(generateSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStock(request.getStock());
        product.setActive(request.getActive());
        product.setFeatured(request.getFeatured());
        product.setCategory(category);
        product.setImages(request.getImages());
        product.setTags(request.getTags());
        product.setBrand(request.getBrand());
        product.setSku(request.getSku());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setMetaKeywords(request.getMetaKeywords());
        
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }
    
    // Update product
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Verify category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Check if new SKU conflicts with existing product
        if (request.getSku() != null && !request.getSku().equals(product.getSku()) && 
            productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        product.setName(request.getName());
        product.setSlug(generateSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStock(request.getStock());
        product.setActive(request.getActive());
        product.setFeatured(request.getFeatured());
        product.setCategory(category);
        product.setImages(request.getImages());
        product.setTags(request.getTags());
        product.setBrand(request.getBrand());
        product.setSku(request.getSku());
        product.setWeight(request.getWeight());
        product.setDimensions(request.getDimensions());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setMetaKeywords(request.getMetaKeywords());
        
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }
    
    // Delete product
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        productRepository.delete(product);
    }
    
    // Update stock
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setStock(quantity);
        productRepository.save(product);
    }
    
    // Helper: Build specification for complex queries
    private Specification<Product> buildSpecification(
            Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            String brand, Double minRating) {
        
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Active products only
            predicates.add(criteriaBuilder.isTrue(root.get("active")));
            
            // Category filter
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            
            // Keyword search
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                var keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), searchPattern)
                );
                predicates.add(keywordPredicate);
            }
            
            // Price range filter
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            // Brand filter
            if (brand != null && !brand.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), brand));
            }
            
            // Rating filter
            if (minRating != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    // Helper: Build product list response
    private ProductListResponse buildProductListResponse(
            Page<Product> productPage, String keyword, Long categoryId, String categoryName,
            BigDecimal minPrice, BigDecimal maxPrice, String brand, String sortBy, String sortDirection) {
        
        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        ProductListResponse.PaginationInfo pagination = new ProductListResponse.PaginationInfo(
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements(),
            productPage.getTotalPages(),
            productPage.hasNext(),
            productPage.hasPrevious()
        );
        
        ProductListResponse.FilterInfo filters = new ProductListResponse.FilterInfo(
            keyword,
            categoryId,
            categoryName,
            minPrice,
            maxPrice,
            brand,
            null, // minRating
            sortBy,
            sortDirection
        );
        
        return new ProductListResponse(products, pagination, filters);
    }
    
    // Helper: Map Product entity to ProductResponse
    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setPrice(product.getPrice());
        response.setDiscountPrice(product.getDiscountPrice());
        response.setStock(product.getStock());
        response.setActive(product.getActive());
        response.setFeatured(product.getFeatured());
        response.setImages(product.getImages());
        response.setTags(product.getTags());
        response.setBrand(product.getBrand());
        response.setSku(product.getSku());
        response.setWeight(product.getWeight());
        response.setDimensions(product.getDimensions());
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setViewCount(product.getViewCount());
        response.setSoldCount(product.getSoldCount());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        // Set category summary
        if (product.getCategory() != null) {
            ProductResponse.CategorySummary categorySummary = new ProductResponse.CategorySummary();
            categorySummary.setId(product.getCategory().getId());
            categorySummary.setName(product.getCategory().getName());
            categorySummary.setImageUrl(product.getCategory().getImageUrl());
            response.setCategory(categorySummary);
        }
        
        // Set computed fields
        response.setInStock(product.isInStock());
        response.setHasDiscount(product.hasDiscount());
        response.setDiscountPercentage(product.getDiscountPercentage());
        response.setEffectivePrice(product.getEffectivePrice());
        
        return response;
    }
    
    // Helper: Generate slug from product name
    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        
        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
    
    // Inner class for price range
    public static class PriceRange {
        public BigDecimal minPrice;
        public BigDecimal maxPrice;
        
        public PriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
}

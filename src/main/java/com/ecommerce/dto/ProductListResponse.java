package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    
    private List<ProductResponse> products;
    private PaginationInfo pagination;
    private FilterInfo filters;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterInfo {
        private String searchKeyword;
        private Long categoryId;
        private String categoryName;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String brand;
        private Double minRating;
        private String sortBy;
        private String sortDirection;
    }
}

package com.ecommerce.repository;

import com.ecommerce.model.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    
    // Find stock history by product
    Page<StockHistory> findByProductId(Long productId, Pageable pageable);
    
    // Find stock history by product (no pagination)
    List<StockHistory> findByProductId(Long productId);
    
    // Find by change type
    Page<StockHistory> findByChangeType(StockHistory.StockChangeType changeType, Pageable pageable);
    
    // Find by product and change type
    List<StockHistory> findByProductIdAndChangeType(Long productId, StockHistory.StockChangeType changeType);
    
    // Find stock history within date range
    List<StockHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find by product and date range
    List<StockHistory> findByProductIdAndCreatedAtBetween(Long productId, LocalDateTime start, LocalDateTime end);
    
    // Find recent stock changes
    List<StockHistory> findTop10ByOrderByCreatedAtDesc();
    
    // Find by changed by user
    Page<StockHistory> findByChangedById(Long userId, Pageable pageable);
    
    // Find by reference (order number, PO, etc.)
    List<StockHistory> findByReference(String reference);
    
    // Count stock changes by product
    Long countByProductId(Long productId);
    
    // Get total stock added by product
    @Query("SELECT SUM(h.changeAmount) FROM StockHistory h WHERE h.product.id = :productId AND h.changeAmount > 0")
    Long getTotalStockAdded(@Param("productId") Long productId);
    
    // Get total stock removed by product
    @Query("SELECT SUM(ABS(h.changeAmount)) FROM StockHistory h WHERE h.product.id = :productId AND h.changeAmount < 0")
    Long getTotalStockRemoved(@Param("productId") Long productId);
}

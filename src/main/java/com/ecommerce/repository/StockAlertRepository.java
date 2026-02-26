package com.ecommerce.repository;

import com.ecommerce.model.StockAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    
    // Find alerts by status
    Page<StockAlert> findByStatus(StockAlert.AlertStatus status, Pageable pageable);
    
    // Find active alerts
    Page<StockAlert> findByStatusOrderByCreatedAtDesc(StockAlert.AlertStatus status, Pageable pageable);
    
    // Find by product
    List<StockAlert> findByProductId(Long productId);
    
    // Find active alert for product
    Optional<StockAlert> findByProductIdAndStatus(Long productId, StockAlert.AlertStatus status);
    
    // Find by alert type
    Page<StockAlert> findByAlertType(StockAlert.AlertType alertType, Pageable pageable);
    
    // Find active alerts by type
    List<StockAlert> findByAlertTypeAndStatus(StockAlert.AlertType alertType, StockAlert.AlertStatus status);
    
    // Count active alerts
    Long countByStatus(StockAlert.AlertStatus status);
    
    // Count active low stock alerts
    Long countByAlertTypeAndStatus(StockAlert.AlertType alertType, StockAlert.AlertStatus status);
    
    // Check if alert exists for product
    boolean existsByProductIdAndStatus(Long productId, StockAlert.AlertStatus status);
}

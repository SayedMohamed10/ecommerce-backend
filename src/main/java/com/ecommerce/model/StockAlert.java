package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;
    
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;
    
    @Column(name = "threshold")
    private Integer threshold; // Low stock threshold
    
    @Column(length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.ACTIVE;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
    
    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Alert Type Enum
    public enum AlertType {
        LOW_STOCK,          // Stock below threshold
        OUT_OF_STOCK,       // Stock = 0
        OVERSTOCK,          // Stock too high (optional)
        REORDER_POINT       // Time to reorder
    }
    
    // Alert Status Enum
    public enum AlertStatus {
        ACTIVE,             // Alert is active
        ACKNOWLEDGED,       // Alert seen by admin
        RESOLVED,           // Alert resolved (stock replenished)
        DISMISSED           // Alert dismissed
    }
}

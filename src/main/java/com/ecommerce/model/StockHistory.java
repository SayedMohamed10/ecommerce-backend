package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;
    
    @Column(name = "new_stock", nullable = false)
    private Integer newStock;
    
    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount; // Can be positive or negative
    
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private StockChangeType changeType;
    
    @Column(length = 500)
    private String reason;
    
    @Column(length = 100)
    private String reference; // Order number, PO number, etc.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy; // Admin who made the change
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Stock Change Type Enum
    public enum StockChangeType {
        PURCHASE,           // Stock added from supplier
        SALE,              // Stock reduced from order
        ADJUSTMENT,        // Manual adjustment (correction, damage, loss)
        RETURN,            // Stock added from return
        RESTOCK,           // Restocking from warehouse
        RESERVATION,       // Stock reserved for pending order
        RELEASE,           // Reserved stock released (order cancelled)
        TRANSFER,          // Stock transfer between warehouses
        DAMAGED,           // Stock marked as damaged/unsellable
        EXPIRED            // Stock expired/removed
    }
}

package com.ecommerce.repository;

import com.ecommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find all items in an order
    List<OrderItem> findByOrderId(Long orderId);
    
    // Find all orders containing a specific product
    List<OrderItem> findByProductId(Long productId);
    
    // Count total quantity sold for a product
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantitySoldByProductId(@Param("productId") Long productId);
    
    // Get best selling products
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product.id " +
           "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();
}

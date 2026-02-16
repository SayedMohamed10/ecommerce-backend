package com.ecommerce.repository;

import com.ecommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByActiveTrue();
    
    List<Category> findByParentIsNull();
    
    List<Category> findByParentIsNullAndActiveTrue();
    
    List<Category> findByParentId(Long parentId);
    
    List<Category> findByParentIdAndActiveTrue(Long parentId);
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllOrdered();
    
    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllActiveOrdered();
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(Long id);
}

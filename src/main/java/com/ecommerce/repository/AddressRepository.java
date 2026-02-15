package com.ecommerce.repository;

import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUser(User user);
    
    List<Address> findByUserId(Long userId);
    
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    void deleteByIdAndUserId(Long id, Long userId);
}

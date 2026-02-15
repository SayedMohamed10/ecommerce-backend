package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.exception.*;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    
    @Transactional
    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);
        
        // Check if email is being changed and if it's already taken
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("Email is already in use");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // Require re-verification
        }
        
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.passwordsMatch()) {
            throw new PasswordMismatchException("New passwords do not match");
        }
        
        User user = getUserByEmail(email);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    public DashboardResponse getDashboard(String email) {
        User user = getUserByEmail(email);
        
        // Build user stats (you'll populate these from actual orders later)
        DashboardResponse.UserStats stats = new DashboardResponse.UserStats();
        stats.setTotalOrders(0); // TODO: Get from OrderRepository
        stats.setTotalSpent(BigDecimal.ZERO); // TODO: Calculate from orders
        stats.setPendingOrders(0); // TODO: Get from OrderRepository
        stats.setCompletedOrders(0); // TODO: Get from OrderRepository
        stats.setSavedItems(0); // TODO: Get from WishlistRepository
        stats.setCartItems(0); // TODO: Get from CartRepository
        stats.setMemberSince(user.getCreatedAt());
        stats.setEmailVerified(user.getEmailVerified());
        stats.setTwoFactorEnabled(user.getTwoFactorEnabled());
        
        // Build recent activities
        List<DashboardResponse.RecentActivity> activities = new ArrayList<>();
        
        // Account created activity
        DashboardResponse.RecentActivity accountCreated = new DashboardResponse.RecentActivity();
        accountCreated.setType("ACCOUNT_CREATED");
        accountCreated.setDescription("Account created successfully");
        accountCreated.setTimestamp(user.getCreatedAt());
        accountCreated.setIcon("user-plus");
        accountCreated.setColor("blue");
        activities.add(accountCreated);
        
        // TODO: Add more activities from orders, reviews, etc.
        
        DashboardResponse response = new DashboardResponse();
        response.setUserStats(stats);
        response.setRecentActivities(activities);
        
        return response;
    }
    
    // Address Management
    
    public List<Address> getUserAddresses(String email) {
        User user = getUserByEmail(email);
        return addressRepository.findByUserId(user.getId());
    }
    
    @Transactional
    public Address addAddress(String email, AddressRequest request) {
        User user = getUserByEmail(email);
        
        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress(request.getStreetAddress());
        address.setApartment(request.getApartment());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());
        address.setType(Address.AddressType.valueOf(request.getType()));
        
        // If this is set as default, unset other default addresses
        if (request.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setIsDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }
        
        return addressRepository.save(address);
    }
    
    @Transactional
    public Address updateAddress(String email, Long addressId, AddressRequest request) {
        User user = getUserByEmail(email);
        
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        address.setStreetAddress(request.getStreetAddress());
        address.setApartment(request.getApartment());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setType(Address.AddressType.valueOf(request.getType()));
        
        // If this is set as default, unset other default addresses
        if (request.getIsDefault() && !address.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(defaultAddress -> {
                        if (!defaultAddress.getId().equals(addressId)) {
                            defaultAddress.setIsDefault(false);
                            addressRepository.save(defaultAddress);
                        }
                    });
            address.setIsDefault(true);
        }
        
        return addressRepository.save(address);
    }
    
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = getUserByEmail(email);
        
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        addressRepository.delete(address);
    }
    
    @Transactional
    public void deleteAccount(String email) {
        User user = getUserByEmail(email);
        
        // Delete user's addresses
        addressRepository.deleteAll(addressRepository.findByUserId(user.getId()));
        
        // TODO: Delete user's orders, reviews, etc.
        
        // Delete user
        userRepository.delete(user);
    }
}

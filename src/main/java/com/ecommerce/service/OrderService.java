package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create order from cart
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get cart items
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        // Validate stock for all items
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            if (!product.getActive()) {
                throw new IllegalStateException("Product '" + product.getName() + "' is no longer available");
            }
            
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for '" + product.getName() + 
                        "'. Available: " + product.getStock() + 
                        ", Requested: " + cartItem.getQuantity());
            }
        }
        
        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        
        // Set payment method
        if (request.getPaymentMethod() != null) {
            try {
                order.setPaymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod()));
            } catch (IllegalArgumentException e) {
                order.setPaymentMethod(Order.PaymentMethod.CASH_ON_DELIVERY);
            }
        }
        
        order.setPaymentTransactionId(request.getPaymentTransactionId());
        
        // Set shipping address
        order.setShippingName(request.getShippingName());
        order.setShippingEmail(request.getShippingEmail());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingAddressLine1(request.getShippingAddressLine1());
        order.setShippingAddressLine2(request.getShippingAddressLine2());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setShippingCountry(request.getShippingCountry());
        order.setOrderNotes(request.getOrderNotes());
        
        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal itemSubtotal = cartItem.getSubtotal();
            subtotal = subtotal.add(itemSubtotal);
            
            // Calculate discount if product has discount
            if (product.hasDiscount()) {
                BigDecimal regularPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                BigDecimal itemDiscount = regularPrice.subtract(itemSubtotal);
                discount = discount.add(itemDiscount);
            }
        }
        
        // For now, tax and shipping are 0 (can be calculated based on location)
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal shippingCost = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).add(shippingCost);
        
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(total);
        
        // Save order first to get ID
        order = orderRepository.save(order);
        
        // Create order items and update product stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductSku(product.getSku());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPriceAtAddition());
            orderItem.setSubtotal(cartItem.getSubtotal());
            
            if (product.hasDiscount()) {
                BigDecimal itemDiscount = product.getPrice()
                        .subtract(product.getDiscountPrice())
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                orderItem.setDiscountAmount(itemDiscount);
            }
            
            order.addOrderItem(orderItem);
            
            // Update product stock and sold count
            product.decrementStock(cartItem.getQuantity());
            product.incrementSoldCount(cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // Save order with items
        order = orderRepository.save(order);
        
        // Clear cart
        cartItemRepository.deleteByUserId(userId);
        
        return mapToResponse(order);
    }
    
    // Get order by ID
    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        return mapToResponse(order);
    }
    
    // Get order by order number
    public OrderResponse getOrderByNumber(Long userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        return mapToResponse(order);
    }
    
    // Get user's order history
    public Page<OrderResponse> getOrderHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::mapToResponse);
    }
    
    // Get recent orders (last 5)
    public List<OrderResponse> getRecentOrders(Long userId) {
        Pageable pageable = PageRequest.of(0, 5);
        List<Order> orders = orderRepository.findRecentOrdersByUserId(userId, pageable);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Continues in Part 2...
// Part 2 of OrderService - Add these methods to the class from Part 1

    // Cancel order
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }
        
        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled. Current status: " + order.getStatus());
        }
        
        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            product.setSoldCount(product.getSoldCount() - item.getQuantity());
            productRepository.save(product);
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);
        
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        }
        
        order = orderRepository.save(order);
        
        return mapToResponse(order);
    }
    
    // Update order status (Admin)
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
            order.setStatus(newStatus);
            
            if (newStatus == Order.OrderStatus.DELIVERED) {
                order.setDeliveredAt(LocalDateTime.now());
            }
            
            order = orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        
        return mapToResponse(order);
    }
    
    // Update payment status (Admin)
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        try {
            Order.PaymentStatus newStatus = Order.PaymentStatus.valueOf(paymentStatus);
            order.setPaymentStatus(newStatus);
            
            if (newStatus == Order.PaymentStatus.PAID) {
                order.setStatus(Order.OrderStatus.CONFIRMED);
            }
            
            order = orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + paymentStatus);
        }
        
        return mapToResponse(order);
    }
    
    // Add tracking number
    @Transactional
    public OrderResponse addTrackingNumber(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setTrackingNumber(trackingNumber);
        order.setStatus(Order.OrderStatus.SHIPPED);
        
        order = orderRepository.save(order);
        
        return mapToResponse(order);
    }
    
    // Get order statistics for user
    public OrderStatistics getUserOrderStatistics(Long userId) {
        Long totalOrders = orderRepository.countByUserId(userId);
        BigDecimal totalSpent = orderRepository.getTotalSalesByUserId(userId);
        
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        
        return new OrderStatistics(
            totalOrders,
            totalSpent,
            totalOrders > 0 ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO
        );
    }
    
    // Generate unique order number
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        String orderNumber = "ORD-" + timestamp + "-" + random;
        
        // Ensure uniqueness
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            random = String.format("%04d", new Random().nextInt(10000));
            orderNumber = "ORD-" + timestamp + "-" + random;
        }
        
        return orderNumber;
    }
    
    // Map Order entity to OrderResponse
    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus().name());
        response.setPaymentStatus(order.getPaymentStatus().name());
        
        if (order.getPaymentMethod() != null) {
            response.setPaymentMethod(order.getPaymentMethod().name());
        }
        
        response.setSubtotal(order.getSubtotal());
        response.setDiscount(order.getDiscount());
        response.setTax(order.getTax());
        response.setShippingCost(order.getShippingCost());
        response.setTotalAmount(order.getTotalAmount());
        
        // Shipping address
        OrderResponse.ShippingAddress shippingAddress = new OrderResponse.ShippingAddress();
        shippingAddress.setName(order.getShippingName());
        shippingAddress.setEmail(order.getShippingEmail());
        shippingAddress.setPhone(order.getShippingPhone());
        shippingAddress.setAddressLine1(order.getShippingAddressLine1());
        shippingAddress.setAddressLine2(order.getShippingAddressLine2());
        shippingAddress.setCity(order.getShippingCity());
        shippingAddress.setState(order.getShippingState());
        shippingAddress.setPostalCode(order.getShippingPostalCode());
        shippingAddress.setCountry(order.getShippingCountry());
        response.setShippingAddress(shippingAddress);
        
        // Order items
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        response.setOrderNotes(order.getOrderNotes());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setCancellationReason(order.getCancellationReason());
        
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCancelledAt(order.getCancelledAt());
        
        return response;
    }
    
    // Map OrderItem to OrderItemResponse
    private OrderResponse.OrderItemResponse mapToItemResponse(OrderItem item) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProductName());
        response.setProductSku(item.getProductSku());
        response.setProductImage(item.getProductImage());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setDiscountAmount(item.getDiscountAmount());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
    
    // Inner class for order statistics
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OrderStatistics {
        private Long totalOrders;
        private BigDecimal totalSpent;
        private BigDecimal averageOrderValue;
    }
}

package com.ecommerce.config;

import com.ecommerce.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key, request);
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendRateLimitError(response, request);
        }
    }
    
    private Bucket resolveBucket(String key, HttpServletRequest request) {
        return cache.computeIfAbsent(key, k -> createNewBucket(request));
    }
    
    private Bucket createNewBucket(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Different limits for different endpoints
        if (path.contains("/login") || path.contains("/signup")) {
            // 5 requests per minute for authentication endpoints
            Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        } else if (path.contains("/forgot-password") || path.contains("/reset-password")) {
            // 3 requests per hour for password reset endpoints
            Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
            return Bucket.builder().addLimit(limit).build();
        } else {
            // 100 requests per minute for other endpoints
            Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        }
    }
    
    private String getClientKey(HttpServletRequest request) {
        // Use IP address as the key
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        
        // Combine IP with endpoint for more granular control
        return clientIp + ":" + request.getRequestURI();
    }
    
    private void sendRateLimitError(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Rate Limit Exceeded",
                "Too many requests. Please try again later.",
                request.getRequestURI()
        );
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}

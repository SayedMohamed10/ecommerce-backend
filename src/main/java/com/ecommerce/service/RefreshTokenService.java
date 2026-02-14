package com.ecommerce.service;

import com.ecommerce.exception.TokenRefreshException;
import com.ecommerce.model.RefreshToken;
import com.ecommerce.model.User;
import com.ecommerce.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    
    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private Long refreshTokenDurationMs;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }
    
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token has expired. Please login again.");
        }
        
        if (token.getRevoked()) {
            throw new TokenRefreshException("Refresh token has been revoked. Please login again.");
        }
        
        return token;
    }
    
    @Transactional
    public int deleteByUser(User user) {
        return refreshTokenRepository.deleteByUser(user);
    }
    
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}

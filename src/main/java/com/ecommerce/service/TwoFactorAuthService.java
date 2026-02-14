package com.ecommerce.service;

import com.ecommerce.dto.TwoFactorSetupResponse;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class TwoFactorAuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    private static final String APP_NAME = "ECommerceApp";
    
    public TwoFactorSetupResponse generateTwoFactorSecret(User user) {
        String secret = Base32.random();
        
        String qrUrl = generateQRUrl(user.getEmail(), secret);
        List<String> backupCodes = generateBackupCodes();
        
        TwoFactorSetupResponse response = new TwoFactorSetupResponse();
        response.setSecret(secret);
        response.setQrCodeUrl(qrUrl);
        response.setBackupCodes(backupCodes);
        response.setMessage("Scan the QR code with Google Authenticator or Authy");
        
        return response;
    }
    
    @Transactional
    public void enable2FA(User user, String secret) {
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }
    
    @Transactional
    public void disable2FA(User user) {
        user.setTwoFactorSecret(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }
    
    public boolean verifyCode(String secret, String code) {
        Totp totp = new Totp(secret);
        return totp.verify(code);
    }
    
    private String generateQRUrl(String email, String secret) {
        String otpauthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                APP_NAME,
                email,
                secret,
                APP_NAME
        );
        
        return QR_PREFIX + URLEncoder.encode(otpauthUrl, StandardCharsets.UTF_8);
    }
    
    private List<String> generateBackupCodes() {
        List<String> backupCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            backupCodes.add(code);
        }
        
        return backupCodes;
    }
}

package com.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Email Verification - E-Commerce App";
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
        
        String body = "Dear User,\n\n" +
                "Thank you for registering with our E-Commerce platform!\n\n" +
                "Please click the link below to verify your email address:\n" +
                verificationUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "E-Commerce Team";
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Password Reset Request - E-Commerce App";
        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;
        
        String body = "Dear User,\n\n" +
                "We received a request to reset your password.\n\n" +
                "Please click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request a password reset, please ignore this email and your password will remain unchanged.\n\n" +
                "Best regards,\n" +
                "E-Commerce Team";
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendAccountLockedEmail(String toEmail) {
        String subject = "Account Locked - Security Alert";
        
        String body = "Dear User,\n\n" +
                "Your account has been locked due to multiple failed login attempts.\n\n" +
                "For security reasons, your account will be automatically unlocked after 30 minutes.\n\n" +
                "If you did not attempt to login, please reset your password immediately.\n\n" +
                "Best regards,\n" +
                "E-Commerce Team";
        
        sendEmail(toEmail, subject, body);
    }
    
    public void sendTwoFactorCode(String toEmail, String code) {
        String subject = "Two-Factor Authentication Code";
        
        String body = "Dear User,\n\n" +
                "Your two-factor authentication code is:\n\n" +
                code + "\n\n" +
                "This code will expire in 5 minutes.\n\n" +
                "If you did not request this code, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "E-Commerce Team";
        
        sendEmail(toEmail, subject, body);
    }
    
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error
            System.err.println("Failed to send email: " + e.getMessage());
            // In production, you might want to throw a custom exception
        }
    }
}

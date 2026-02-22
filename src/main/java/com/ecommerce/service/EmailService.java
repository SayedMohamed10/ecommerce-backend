package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@ecommerce.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;
    
    // Send welcome email
    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to Our E-Commerce Store!";
        String body = "<h1>Welcome " + user.getName() + "!</h1><p>Thank you for joining us.</p>";
        sendHtmlEmail(user.getEmail(), subject, body);
    }
    
    // Send order confirmation email
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        String subject = "Order Confirmation - " + order.getOrderNumber();
        String body = "<h1>Order Confirmed!</h1>" +
                "<p>Thank you for your order, " + order.getUser().getName() + "!</p>" +
                "<p>Order Number: " + order.getOrderNumber() + "</p>" +
                "<p>Total: $" + order.getTotalAmount() + "</p>";
        sendHtmlEmail(order.getUser().getEmail(), subject, body);
    }
    
    // Send order shipped email
    @Async
    public void sendOrderShippedEmail(Order order) {
        String subject = "Your Order Has Been Shipped - " + order.getOrderNumber();
        String body = "<h1>Order Shipped!</h1>" +
                "<p>Your order " + order.getOrderNumber() + " is on the way!</p>" +
                "<p>Tracking: " + order.getTrackingNumber() + "</p>";
        sendHtmlEmail(order.getUser().getEmail(), subject, body);
    }
    
    // Generic method to send HTML email
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}

package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.exception.*;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        // Check if passwords match
        if (!signupRequest.passwordsMatch()) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }
        
        // Create new user
        User user = new User();
        user.setName(signupRequest.getName().trim());
        user.setEmail(signupRequest.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setTwoFactorEnabled(false);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = new EmailVerificationToken();
        emailToken.setToken(verificationToken);
        emailToken.setUser(savedUser);
        emailToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        emailToken.setVerified(false);
        emailVerificationTokenRepository.save(emailToken);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(savedUser.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);
        
        return new AuthResponse(accessToken, refreshToken.getToken(), savedUser);
    }
    
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new LockedException("Account is locked due to too many failed login attempts. " +
                    "Please try again later or reset your password.");
        }
        
        // Check if user is enabled
        if (!user.getEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }
        
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail().toLowerCase().trim(),
                            loginRequest.getPassword()
                    )
            );
            
            // Reset failed attempts on successful login
            user.resetFailedAttempts();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Check if 2FA is enabled
            if (user.getTwoFactorEnabled()) {
                if (loginRequest.getTwoFactorCode() == null || loginRequest.getTwoFactorCode().isEmpty()) {
                    // Return response indicating 2FA is required
                    AuthResponse response = new AuthResponse();
                    response.setRequiresTwoFactor(true);
                    response.setMessage("Two-factor authentication code required");
                    return response;
                }
                
                // Verify 2FA code
                boolean isValid = twoFactorAuthService.verifyCode(
                        user.getTwoFactorSecret(),
                        loginRequest.getTwoFactorCode()
                );
                
                if (!isValid) {
                    throw new InvalidCredentialsException("Invalid two-factor authentication code");
                }
            }
            
            // Generate tokens
            String accessToken = jwtUtil.generateToken(user.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            
            return new AuthResponse(accessToken, refreshToken.getToken(), user);
            
        } catch (BadCredentialsException e) {
            // Increment failed attempts
            user.incrementFailedAttempts();
            
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
                emailService.sendAccountLockedEmail(user.getEmail());
            }
            
            userRepository.save(user);
            
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
    
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
        
        refreshTokenService.verifyExpiration(refreshToken);
        
        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getEmail());
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshTokenStr);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setRole(user.getRole().name());
        
        return response;
    }
    
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
    
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
        
        if (verificationToken.isExpired()) {
            throw new InvalidTokenException("Verification token has expired");
        }
        
        if (verificationToken.getVerified()) {
            throw new InvalidTokenException("Email already verified");
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        verificationToken.setVerified(true);
        emailVerificationTokenRepository.save(verificationToken);
    }
    
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (user.getEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }
        
        // Delete old verification tokens
        emailVerificationTokenRepository.deleteByUser(user);
        
        // Generate new token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = new EmailVerificationToken();
        emailToken.setToken(verificationToken);
        emailToken.setUser(user);
        emailToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        emailToken.setVerified(false);
        emailVerificationTokenRepository.save(emailToken);
        
        // Send email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
    
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Delete old reset tokens
        passwordResetTokenRepository.deleteByUser(user);
        
        // Generate new token
        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken passwordToken = new PasswordResetToken();
        passwordToken.setToken(resetToken);
        passwordToken.setUser(user);
        passwordToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordToken.setUsed(false);
        passwordResetTokenRepository.save(passwordToken);
        
        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.passwordsMatch()) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));
        
        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Password reset token has expired");
        }
        
        if (resetToken.getUsed()) {
            throw new InvalidTokenException("Password reset token has already been used");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedAttempts(); // Unlock account if locked
        userRepository.save(user);
        
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
    
    @Transactional
    public TwoFactorSetupResponse setup2FA(User user) {
        if (user.getTwoFactorEnabled()) {
            throw new IllegalStateException("Two-factor authentication is already enabled");
        }
        
        return twoFactorAuthService.generateTwoFactorSecret(user);
    }
    
    @Transactional
    public void enable2FA(User user, String secret, String code) {
        boolean isValid = twoFactorAuthService.verifyCode(secret, code);
        
        if (!isValid) {
            throw new InvalidCredentialsException("Invalid verification code");
        }
        
        twoFactorAuthService.enable2FA(user, secret);
    }
    
    @Transactional
    public void disable2FA(User user, String code) {
        if (!user.getTwoFactorEnabled()) {
            throw new IllegalStateException("Two-factor authentication is not enabled");
        }
        
        boolean isValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
        
        if (!isValid) {
            throw new InvalidCredentialsException("Invalid verification code");
        }
        
        twoFactorAuthService.disable2FA(user);
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

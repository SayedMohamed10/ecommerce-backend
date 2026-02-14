# E-Commerce Backend - Complete Authentication System

A production-ready Spring Boot backend with advanced authentication features including email verification, password reset, refresh tokens, OAuth2 social login, rate limiting, account lockout, and two-factor authentication (2FA).

## 🚀 Features

### ✅ Core Authentication
- User signup with validation
- User login with JWT tokens
- Logout functionality
- Token refresh mechanism

### ✅ Email Verification
- Email verification on signup
- Verification email with clickable link
- Token expiration (24 hours)
- Resend verification email

### ✅ Password Reset
- Forgot password functionality
- Password reset via email link
- Reset token expiration (1 hour)
- Secure password update

### ✅ Refresh Tokens
- Long-lived refresh tokens (7 days)
- Short-lived access tokens (15 minutes)
- Token revocation on logout
- Automatic token cleanup

### ✅ OAuth2 Social Login
- Login with Google
- Login with GitHub
- Login with Facebook
- Automatic account creation

### ✅ Rate Limiting
- 5 login attempts per minute
- 3 password reset requests per hour
- 100 general API requests per minute
- IP-based rate limiting

### ✅ Account Lockout
- Lock after 5 failed login attempts
- Automatic unlock after 30 minutes
- Email notification on lockout
- Failed attempts counter

### ✅ Two-Factor Authentication (2FA)
- TOTP-based authentication
- QR code generation for authenticator apps
- Backup codes generation
- Enable/disable 2FA functionality

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Gmail account (for sending emails)
- OAuth2 credentials (optional, for social login)

## 🛠️ Installation

### 1. Database Setup

```sql
CREATE DATABASE ecommerce_db;
```

### 2. Configure Database

Update `src/main/resources/application.properties`:

```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 3. Configure Email (Gmail)

**Get Gmail App Password:**
1. Go to Google Account Settings
2. Security → 2-Step Verification → App Passwords
3. Generate password for "Mail"

**Update application.properties:**
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-digit-app-password
```

### 4. Configure OAuth2 (Optional)

**Google OAuth2:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create project → APIs & Services → Credentials
3. Create OAuth 2.0 Client ID
4. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

```properties
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
```

**GitHub OAuth2:**
1. Go to GitHub Settings → Developer settings → OAuth Apps
2. Create new OAuth App
3. Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`

```properties
spring.security.oauth2.client.registration.github.client-id=your-client-id
spring.security.oauth2.client.registration.github.client-secret=your-client-secret
```

### 5. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

Server will start on `http://localhost:8080`

## 📚 API Documentation

### Authentication Endpoints

#### 1. Signup
```http
POST /api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "phoneNumber": "1234567890"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER",
  "emailVerified": false,
  "twoFactorEnabled": false
}
```

#### 2. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!",
  "twoFactorCode": "123456"
}
```

#### 3. Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 4. Logout
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Email Verification Endpoints

#### 5. Verify Email
```http
GET /api/auth/verify-email?token=abc123xyz
```

#### 6. Resend Verification Email
```http
POST /api/auth/resend-verification
Content-Type: application/json

{
  "email": "john@example.com"
}
```

### Password Reset Endpoints

#### 7. Request Password Reset
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### 8. Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

### Two-Factor Authentication Endpoints

#### 9. Setup 2FA
```http
POST /api/auth/2fa/setup
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCodeUrl": "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=...",
  "backupCodes": ["12345678", "87654321", ...],
  "message": "Scan the QR code with Google Authenticator or Authy"
}
```

#### 10. Enable 2FA
```http
POST /api/auth/2fa/enable?secret=JBSWY3DPEHPK3PXP
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "code": "123456"
}
```

#### 11. Disable 2FA
```http
POST /api/auth/2fa/disable
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "code": "123456"
}
```

### User Profile Endpoints

#### 12. Get Current User
```http
GET /api/auth/me
Authorization: Bearer {access_token}
```

#### 13. Validate Token
```http
GET /api/auth/validate
Authorization: Bearer {access_token}
```

## 🔐 Security Features

### Password Requirements
- Minimum 8 characters
- Maximum 100 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### Rate Limiting
- **Login/Signup:** 5 requests per minute per IP
- **Password Reset:** 3 requests per hour per IP
- **General API:** 100 requests per minute per IP

### Account Lockout
- Locks after 5 failed login attempts
- Automatically unlocks after 30 minutes
- Email notification sent on lockout
- Can be unlocked via password reset

### Token Expiration
- **Access Token:** 15 minutes
- **Refresh Token:** 7 days
- **Email Verification Token:** 24 hours
- **Password Reset Token:** 1 hour

## 📧 Email Templates

The system sends emails for:
1. Email verification on signup
2. Password reset requests
3. Account lockout notifications
4. Two-factor authentication codes (if using email-based 2FA)

## 🗄️ Database Schema

### Users Table
```sql
- id (PRIMARY KEY)
- name
- email (UNIQUE)
- password
- phone_number
- role (USER, ADMIN)
- enabled
- email_verified
- account_locked
- failed_login_attempts
- lockout_time
- two_factor_enabled
- two_factor_secret
- oauth_provider
- oauth_id
- created_at
- updated_at
- last_login
```

### Refresh Tokens Table
```sql
- id (PRIMARY KEY)
- token (UNIQUE)
- user_id (FOREIGN KEY)
- expiry_date
- created_at
- revoked
```

### Email Verification Tokens Table
```sql
- id (PRIMARY KEY)
- token (UNIQUE)
- user_id (FOREIGN KEY)
- expiry_date
- created_at
- verified
```

### Password Reset Tokens Table
```sql
- id (PRIMARY KEY)
- token (UNIQUE)
- user_id (FOREIGN KEY)
- expiry_date
- created_at
- used
```

## 🧪 Testing

### Test Signup
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "TestPass123!",
    "confirmPassword": "TestPass123!",
    "phoneNumber": "1234567890"
  }'
```

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123!"
  }'
```

### Test with 2FA
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123!",
    "twoFactorCode": "123456"
  }'
```

## 🔧 Configuration

### Customizing Rate Limits

Edit `application.properties`:
```properties
app.rate-limit.login-per-minute=10
app.rate-limit.password-reset-per-hour=5
app.rate-limit.general-per-minute=200
```

### Customizing Account Lockout

Edit `application.properties`:
```properties
app.security.max-failed-attempts=3
app.security.lockout-duration-minutes=60
```

### Customizing Token Expiration

Edit `application.properties`:
```properties
jwt.expiration=1800000
jwt.refresh-expiration=1209600000
```

## 🚨 Error Responses

### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2024-02-14T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/auth/signup",
  "details": [
    "password: Password must contain at least one uppercase letter"
  ]
}
```

### 401 Unauthorized - Invalid Credentials
```json
{
  "timestamp": "2024-02-14T10:30:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid email or password",
  "path": "/api/auth/login"
}
```

### 423 Locked - Account Locked
```json
{
  "timestamp": "2024-02-14T10:30:00",
  "status": 423,
  "error": "Account Locked",
  "message": "Account is locked due to too many failed login attempts",
  "path": "/api/auth/login"
}
```

### 429 Too Many Requests - Rate Limit Exceeded
```json
{
  "timestamp": "2024-02-14T10:30:00",
  "status": 429,
  "error": "Rate Limit Exceeded",
  "message": "Too many requests. Please try again later.",
  "path": "/api/auth/login"
}
```

## 📱 Two-Factor Authentication Setup

### Using Google Authenticator

1. Call `/api/auth/2fa/setup` endpoint
2. Scan the QR code with Google Authenticator app
3. Enter the 6-digit code from the app
4. Call `/api/auth/2fa/enable` with the code
5. Save the backup codes securely

### Using Authy

Same process as Google Authenticator, works with any TOTP-compatible app.

## 🔄 OAuth2 Login Flow

### Google Login
1. Frontend redirects to: `GET /oauth2/authorization/google`
2. User authenticates with Google
3. Google redirects back with code
4. Backend creates/logs in user
5. Returns JWT tokens

### GitHub Login
1. Frontend redirects to: `GET /oauth2/authorization/github`
2. User authenticates with GitHub
3. GitHub redirects back with code
4. Backend creates/logs in user
5. Returns JWT tokens

## 📊 Project Structure

```
src/main/java/com/ecommerce/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── RateLimitFilter.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── SignupRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── RefreshTokenRequest.java
│   ├── PasswordResetRequest.java
│   ├── ResetPasswordRequest.java
│   ├── TwoFactorVerifyRequest.java
│   ├── TwoFactorSetupResponse.java
│   └── ErrorResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── EmailAlreadyExistsException.java
│   ├── PasswordMismatchException.java
│   ├── InvalidCredentialsException.java
│   ├── InvalidTokenException.java
│   ├── TokenRefreshException.java
│   └── UserNotFoundException.java
├── model/
│   ├── User.java
│   ├── RefreshToken.java
│   ├── EmailVerificationToken.java
│   └── PasswordResetToken.java
├── repository/
│   ├── UserRepository.java
│   ├── RefreshTokenRepository.java
│   ├── EmailVerificationTokenRepository.java
│   └── PasswordResetTokenRepository.java
├── service/
│   ├── AuthService.java
│   ├── CustomUserDetailsService.java
│   ├── EmailService.java
│   ├── RefreshTokenService.java
│   └── TwoFactorAuthService.java
├── util/
│   └── JwtUtil.java
└── validation/
    ├── ValidPassword.java
    └── PasswordValidator.java
```

## 🐛 Troubleshooting

### Email Not Sending
- Check Gmail app password is correct
- Enable "Less secure app access" if needed
- Check spam folder

### OAuth2 Not Working
- Verify redirect URIs match exactly
- Check client ID and secret
- Ensure OAuth consent screen is configured

### Account Locked
- Wait 30 minutes for auto-unlock
- Or use password reset to unlock immediately

### 2FA Not Working
- Ensure time is synchronized on both devices
- Use backup codes if code doesn't work
- QR code must be scanned correctly

## 🚀 Deployment

### Production Checklist
- [ ] Change JWT secret to a secure random string
- [ ] Use environment variables for sensitive data
- [ ] Set up proper HTTPS
- [ ] Configure CORS for your frontend domain
- [ ] Set up proper email service (not Gmail)
- [ ] Enable production logging
- [ ] Set up database backups
- [ ] Configure rate limiting appropriately
- [ ] Test all OAuth2 redirects with production URLs

## 📝 License

MIT License

## 👥 Contributors

Your Name - Initial work

## 📞 Support

For issues, please open a GitHub issue or contact support.

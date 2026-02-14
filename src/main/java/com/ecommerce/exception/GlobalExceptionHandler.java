package com.ecommerce.exception;

import com.ecommerce.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid input data",
                request.getRequestURI(),
                errors
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Email Already Exists",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(
            PasswordMismatchException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Password Mismatch",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            LockedException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.LOCKED.value(),
                "Account Locked",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);
    }
    
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(
            TokenRefreshException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token Refresh Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Token",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Operation",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        
        // Log the exception
        ex.printStackTrace();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.ecommerce.dto;

import com.ecommerce.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "Password is required")
    @ValidPassword
    private String newPassword;
    
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}

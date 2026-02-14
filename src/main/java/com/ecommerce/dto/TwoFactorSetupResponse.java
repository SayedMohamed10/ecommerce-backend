package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupResponse {
    
    private String secret;
    private String qrCodeUrl;
    private List<String> backupCodes;
    private String message;
}

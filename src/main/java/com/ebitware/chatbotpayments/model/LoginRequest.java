package com.ebitware.chatbotpayments.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String encryptedCredentials;
}

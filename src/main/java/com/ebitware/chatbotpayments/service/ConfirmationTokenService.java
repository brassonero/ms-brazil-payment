package com.ebitware.chatbotpayments.service;

public interface ConfirmationTokenService {
    String generateToken();
    void saveToken(String email, String token);
    String validateToken(String token);

}

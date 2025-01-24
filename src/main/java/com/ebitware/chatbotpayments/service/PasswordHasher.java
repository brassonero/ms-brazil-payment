package com.ebitware.chatbotpayments.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    @Async
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
}

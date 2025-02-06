package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.service.ConfirmationTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {

    private final FormSubmissionRepository formSubmissionRepository;

    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void saveToken(String email, String token) {
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);
        formSubmissionRepository.saveConfirmationToken(email, token, expiryTime);
    }

    @Override
    public String validateToken(String token) {
        return formSubmissionRepository.validateToken(token);
    }
}

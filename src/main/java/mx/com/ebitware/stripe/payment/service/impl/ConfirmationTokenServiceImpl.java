package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.repository.FormSubmissionRepository;
import mx.com.ebitware.stripe.payment.service.ConfirmationTokenService;
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

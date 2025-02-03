package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.FormSubmissionRequest;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.service.ConfirmationTokenService;
import com.ebitware.chatbotpayments.service.EmailService;
import com.ebitware.chatbotpayments.service.FormSubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private final FormSubmissionRepository formSubmissionRepository;
    private final EmailService emailService;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    @Transactional
    public void saveSubmission(FormSubmissionRequest form, String logoUrl) {

        formSubmissionRepository.saveSubmissionForm(form, logoUrl);
/*
        String token = confirmationTokenService.generateToken();
        confirmationTokenService.saveToken(form.getCorporateEmail(), token);

        String confirmationUrl = "https://management-dev.broadcasterbot.com/paymentsApi/email/confirm?token=" + token;
        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "Thank you for your submission. Please click the link below to confirm your email:\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "Best regards,\n" +
                        "Your Application Team",
                form.getDisplayName(),
                confirmationUrl
        );

        emailService.sendEmail(
                form.getCorporateEmail(),
                "Confirm your email address",
                emailBody
        );
 */
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !formSubmissionRepository.emailExists(email);
    }
}

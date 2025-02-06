package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.FormSubmissionRequest;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.service.FormSubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private final FormSubmissionRepository formSubmissionRepository;

    @Override
    @Transactional
    public void saveSubmission(FormSubmissionRequest form, String logoUrl) {
        formSubmissionRepository.saveSubmissionForm(form, logoUrl);
    }
}

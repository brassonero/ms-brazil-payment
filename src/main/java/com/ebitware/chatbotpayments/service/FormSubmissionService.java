package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.FormSubmissionRequest;

public interface FormSubmissionService {
    void saveSubmission(FormSubmissionRequest form, String logoUrl);
}

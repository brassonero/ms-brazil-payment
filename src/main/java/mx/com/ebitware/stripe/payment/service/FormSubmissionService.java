package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.FormSubmissionRequest;

public interface FormSubmissionService {
    void saveSubmission(FormSubmissionRequest form, String logoUrl);
}

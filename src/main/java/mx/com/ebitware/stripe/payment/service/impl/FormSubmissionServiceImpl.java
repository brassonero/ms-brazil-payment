package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.FormSubmissionRequest;
import mx.com.ebitware.stripe.payment.repository.FormSubmissionRepository;
import mx.com.ebitware.stripe.payment.service.FormSubmissionService;
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

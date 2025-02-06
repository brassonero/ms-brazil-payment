package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.CompanyModeEnum;
import com.ebitware.chatbotpayments.model.WorkspaceDTO;
import com.ebitware.chatbotpayments.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl {

    private final PersonRepository personRepository;
    private final UtilService utilService;
    private final EmailServiceImpl emailServiceImpl;

    @Value("${app.name:}")
    private String appName;

    @Transactional
    public Long createCompany(WorkspaceDTO request) {
        validateRequest(request);

        String password = appName + utilService.randomString(8);
        String username = request.getUser().getEmail().split("@")[0];
        log.info("Generated password for user {}: {}", request.getUser().getEmail(), password);

        Long companyId = personRepository.createCompany(request, password);

        try {
            String subject = "Registro en el sistema";
            String body = String.format("""
                Your account has been created successfully.
                                
                Username: %s
                Password: %s
                
                Please keep these credentials safe.
                """, username, password);

            emailServiceImpl.sendEmail(request.getUser().getEmail(), subject, body);
            log.info("Credentials email sent to: {}", request.getUser().getEmail());

        } catch (Exception e) {
            log.error("Error sending credentials email to: {}", request.getUser().getEmail(), e);
        }

        return companyId;
    }

    private void validateRequest(WorkspaceDTO request) {
        if (personRepository.existsByEmail(request.getUser().getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getUser().getEmail());
        }

        if (CompanyModeEnum.PLATFORM == CompanyModeEnum.fromCode(request.getMode())
                && (request.getAccessList() == null || request.getAccessList().isEmpty())) {
            throw new RuntimeException("The company needs at least one access");
        }
    }
}

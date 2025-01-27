package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.*;
import com.ebitware.chatbotpayments.repository.CompanyRepository;
import com.ebitware.chatbotpayments.service.CompanyService;
import com.ebitware.chatbotpayments.service.EmailService;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final StringUtil stringUtil;
    private final EmailService emailService;

    @Value("${app.name:}")
    private String appName;

    @Override
    @Transactional
    public Long createCompany(WorkspaceDTO request) {
        validateRequest(request);
        String password = generatePassword();
        String username = createUsername(request.getUser());
        log.info("Generated password for user {}: {}", request.getUser().getEmail(), password);

        Long companyId = companyRepository.createCompany(request, password, username);
        sendWelcomeEmail(request.getUser().getEmail(), username, password);

        return companyId;
    }

    private String generatePassword() {
        return appName + stringUtil.randomString(8, "both");
    }

    private String createUsername(UserDTO user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String secondLastName = user.getSecondLastName();

        if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
            throw new RuntimeException("First name and last name are required");
        }

        String baseUsername = generateBaseUsername(firstName, lastName, secondLastName);
        return getUniqueUsername(baseUsername);
    }

    private String generateBaseUsername(String firstName, String lastName, String secondLastName) {
        String username = firstName.substring(0, 1) +
                lastName +
                (StringUtils.isNotBlank(secondLastName) ? secondLastName.substring(0, 1) : "");

        return stringUtil.removeAccents(username.toLowerCase());
    }

    private String getUniqueUsername(String baseUsername) {
        List<String> existingUsernames = companyRepository.findUsernamesLike(baseUsername);

        if (existingUsernames.isEmpty()) {
            return baseUsername;
        }

        int counter = 1;
        String username = baseUsername;
        while (existingUsernames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(username.toLowerCase())) {
            username = baseUsername + counter++;
        }
        return username;
    }

    private void sendWelcomeEmail(String email, String username, String password) {
        try {
            emailService.sendEmail(
                    email,
                    "Account Registration",
                    createWelcomeEmailBody(username, password)
            );
            log.info("Credentials email sent to: {}", email);
        } catch (Exception e) {
            log.error("Error sending credentials email to: {}", email, e);
        }
    }

    private String createWelcomeEmailBody(String username, String password) {
        return String.format("""
            Your account has been created successfully.
            
            Username: %s
            Password: %s
            
            Please keep these credentials safe.""",
                username, password);
    }

    private void validateRequest(WorkspaceDTO request) {
        if (companyRepository.existsByEmail(request.getUser().getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getUser().getEmail());
        }

        if (companyRepository.existsByCompanyName(request.getName())) {
            throw new RuntimeException("Company name already exists: " + request.getName());
        }

        if (CompanyModeEnum.PLATFORM == CompanyModeEnum.fromCode(request.getMode())
                && (request.getAccessList() == null || request.getAccessList().isEmpty())) {
            throw new RuntimeException("Platform mode requires at least one access");
        }
    }
}

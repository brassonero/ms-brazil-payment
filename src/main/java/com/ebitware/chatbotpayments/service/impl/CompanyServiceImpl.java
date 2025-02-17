package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.*;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.repository.chatbot.CompanyRepository;
import com.ebitware.chatbotpayments.service.CompanyService;
import com.ebitware.chatbotpayments.service.EmailService;
import com.ebitware.chatbotpayments.util.StringUtil;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final StringUtil stringUtil;
    private final EmailService emailService;
    private final FormSubmissionRepository formSubmissionRepository;

    @Value("${app.name:}")
    private String appName;

    @Override
    @Transactional
    public Long createCompany(WorkspaceDTO request) {
        validateRequest(request);
        String password = generatePassword();
        String username = createUsername(request.getUser());
        log.info("Generated password for user {}: {}", request.getUser().getEmail(), password);

        CompanyCreationResult result = companyRepository.createCompany(request, password, username);

        try {
            formSubmissionRepository.updateFormSubmissionIds(
                    result.getCompanyId(),
                    result.getPersonId(),
                    result.getRoleId(),
                    request.getUser().getEmail()
            );
        } catch (Exception e) {
            log.error("Error updating form submission with IDs for company {}: {}",
                    result.getCompanyId(), e.getMessage());
        }

        sendWelcomeEmail(request.getUser().getEmail(), username, password);

        return result.getCompanyId();
    }

    @Override
    public Map<String, Object> getCompanyDetails(Long companyId) {
        Map<String, Object> companyDetails = companyRepository.getCompanyById(companyId);
        Map<String, Object> userDetails = companyRepository.getUserByCompanyId(companyId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", companyDetails.get("id"));
        response.put("active", companyDetails.get("active"));
        response.put("createdAt", companyDetails.get("created_at"));
        response.put("updatedAt", companyDetails.get("updated_at"));
        response.put("user", userDetails);

        return response;
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
            String subject = "Bem-vindo à CM Móvil!";
            String htmlContent = createWelcomeEmailBody(username, password);

            emailService.sendEmail(email, subject, htmlContent, "text/html; charset=UTF-8");
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", email, e);
        }
    }

    private String createWelcomeEmailBody(String username, String password) {
        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Bem-vindo à CM Móvil!</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; padding: 20px;">
                <tr>
                    <td style="background-color: #ffffff; padding: 30px; border: 1px solid #cccccc; border-radius: 8px;">
                        <!-- Header with logos -->
                        <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                                <td align="center" style="padding-bottom: 30px;">
                                    <img src="https://raw.githubusercontent.com/brassonero/img/refs/heads/main/dace1567fff6a7303ee73afdd202c846.png" alt="Broadcaster Bot Logo" style="max-width: 200px; height: auto; margin-right: 20px;">
                                    <img src="https://raw.githubusercontent.com/brassonero/img/refs/heads/main/da1317b30912d518e73aa6b22d1407dc.png" alt="Meta Business Partners Logo" style="max-width: 200px; height: auto;">
                                </td>
                            </tr>
                        </table>

                        <!-- Content -->
                        <h2 style="color: #333333; margin-bottom: 20px;">Bem-vindo à CM Móvil!</h2>
                        
                        <p style="color: #333333; line-height: 1.5;">Estamos muito felizes por você ter decidido se juntar a nós. Sua compra Broadcaster Bot foi bem-sucedida, e agora você faz parte de nossa comunidade.</p>
                        
                        <p style="color: #333333; line-height: 1.5;">Queremos que você aproveite ao máximo a plataforma, tornando sua estratégia de retenção de clientes mais eficiente, economizando custos e potencializando suas vendas.</p>
                        
                        <p style="color: #333333; line-height: 1.5;">Seu processo de onboarding já começou. Nos próximos dias, nossa equipe operacional entrará em contato com você para dar continuidade ao processo de ativação da sua conta e realizar as configurações necessárias.</p>
                        
                        <p style="color: #333333; line-height: 1.5;">Enquanto isso, estamos compartilhando seus dados básicos de login para que você possa continuar com o processo de finalização da compra.</p>
                        
                        <!-- Login Info Box -->
                        <div style="background-color: #f8f9fa; padding: 15px; border-radius: 4px; margin: 20px 0; text-align: center;">
                            <p style="margin: 5px 0; color: #333333;">Usuário: %s</p>
                            <p style="margin: 5px 0; color: #333333;">Senha: %s</p>
                        </div>
                        
                        <!-- Buttons -->
                        <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                                <td align="center" style="padding: 20px 0;">
                                    <a href="https://soporte.broadcasterbot.com/support/home" target="_blank" style="display: inline-block; margin: 0 10px; padding: 10px 20px; border: 2px solid #ff5722; border-radius: 4px; color: #ff5722; text-decoration: none; font-weight: bold;">Tutoriais em PDF</a>
                                    <a href="https://admin-dev.broadcasterbot.com/mx/login" target="_blank" style="display: inline-block; margin: 0 10px; padding: 10px 20px; background-color: #ff5722; border-radius: 4px; color: #ffffff; text-decoration: none; font-weight: bold;">Conecte-se</a>
                                </td>
                            </tr>
                        </table>
                        
                        <!-- Support Text -->
                        <div style="margin-top: 20px; color: #333333;">
                            <p style="line-height: 1.5;">Se você tiver alguma dúvida ou precisar de suporte, não hesite em nos contatar pelo <a href="mailto:oym@conceptomovil.com" style="color: #ff5722; text-decoration: none;">oym@conceptomovil.com</a>.</p>
                            <p style="line-height: 1.5;">Estamos aqui para ajudar.</p>
                            <p style="line-height: 1.5;">Para mais informações visite <a href="http://www.conceptomovil.com" style="color: #ff5722; text-decoration: none;">www.conceptomovil.com</a>.</p>
                            <p style="line-height: 1.5;">Somos parceiros oficiais do WhatsApp Business!</p>
                        </div>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """, username, password);
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

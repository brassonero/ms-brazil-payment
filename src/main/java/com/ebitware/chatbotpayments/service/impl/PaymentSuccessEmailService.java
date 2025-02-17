package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.PaymentSuccessEvent;
import com.ebitware.chatbotpayments.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentSuccessEmailService {
    private final EmailService emailService;

    private static final String RECIPIENT_EMAIL = "jamador@e-bitware.com.mx";

    @Value("${app.support.email}")
    private String supportEmail;

    @Value("${app.support.hours}")
    private String supportHours;

    @Value("${app.website.url}")
    private String websiteUrl;

    public void sendPaymentSuccessEmails(PaymentSuccessEvent event) {
        try {
            event = updateAgentsBasedOnPlan(event);
            sendInternalNotification(event);
            sendCustomerConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to send payment success emails", e);
            throw new RuntimeException("Failed to send payment success emails", e);
        }
    }

    private PaymentSuccessEvent updateAgentsBasedOnPlan(PaymentSuccessEvent event) {
        if ("One-Time".equals(event.getContractPeriod())) {
            return rebuildEventWithAgents(event, "N/A", 0);  // The 0 won't matter as it'll be displayed as "N/A"
        }

        String planName = event.getPlanName();
        if (planName == null || planName.trim().isEmpty()) {
            throw new RuntimeException("Plan name is required");
        }

        int agents = switch (planName.toLowerCase()) {
            case "growth" -> 5;
            case "business" -> 10;
            case "enterprise" -> 15;
            default -> throw new RuntimeException("Invalid plan name: " + planName);
        };

        return rebuildEventWithAgents(event, planName, agents);
    }

    private PaymentSuccessEvent rebuildEventWithAgents(PaymentSuccessEvent event, String planName, int agents) {
        boolean isOneTime = "One-Time".equals(event.getContractPeriod());

        return PaymentSuccessEvent.builder()
                .companyName(event.getCompanyName())
                .bmId(event.getBmId())
                .commercialName(event.getCommercialName())
                .phone(event.getPhone())
                .email(event.getEmail())
                .website(event.getWebsite())
                .address(event.getAddress())
                .vertical(event.getVertical())
                .businessDescription(event.getBusinessDescription())
                .planName(isOneTime ? "N/A" : planName)
                .planValue(event.getPlanValue())
                .currency(event.getCurrency())
                .contractPeriod(event.getContractPeriod())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .agents(isOneTime ? "N/A" : String.valueOf(agents))
                .addons(event.getAddons())
                .monthlyVolume(event.getMonthlyVolume())
                .channels(event.getChannels())
                .paymentDate(event.getPaymentDate())
                .setupFee(event.getSetupFee())
                .customerEmail(event.getEmail())
                .build();
    }

    private void sendInternalNotification(PaymentSuccessEvent event) {
        String subject = "Novo Cliente - Confirmação de Pagamento";
        String body = buildInternalEmailBody(event);
        emailService.sendEmail(RECIPIENT_EMAIL, subject, body, "text/html");
    }

    private void sendCustomerConfirmation(PaymentSuccessEvent event) {
        String subject = "Bem-vindo à CM Móvil!";
        String body = buildCustomerEmailBody(event);
        emailService.sendEmail(event.getEmail(), subject, body, "text/html");
    }

    private String buildInternalEmailBody(PaymentSuccessEvent event) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="color: #000000;">
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
                <h2 style="color: #000000;">Estimado equipo de OYM,</h2>
                
                <p style="color: #000000;">Espero que este correo los encuentre bien. Les escribo para informar que hemos recibido la 
                confirmación del pago del setup para un nuevo cliente. A continuación, comparto todos los datos 
                necesarios para iniciar el proceso de onboarding:</p>
                
                <h3 style="color: #FF5722;">Datos del Cliente</h3>
                <ul>
                    <li style="color: #000000;">Empresa: %s</li>
                    <li style="color: #000000;">BM ID: %s</li>
                    <li style="color: #000000;">Nombre Comercial: %s</li>
                </ul>
                
                <h3 style="color: #000000;">Contacto</h3>
                <ul>
                    <li style="color: #000000;">Teléfono: %s</li>
                    <li style="color: #000000;">Email Corporativo: %s</li>
                    <li style="color: #000000;">Sitio Web: %s</li>
                </ul>
                
                <h3 style="color: #000000;">Ubicación</h3>
                <ul>
                    <li style="color: #000000;">Dirección: %s</li>
                    <li style="color: #000000;">Información Comercial:</li>
                    <ul>
                        <li style="color: #000000;">Vertical: %s</li>
                    </ul>
                </ul>
                
                <h3 style="color: #000000;">Descripción del Negocio</h3>
                <p style="color: #000000;">%s</p>
                
                <h3 style="color: #FF5722;">Datos de Compra</h3>
                <ul>
                    <li style="color: #000000;">Plan: %s</li>
                    <li style="color: #000000;">Valor: %s %s</li>
                    <li style="color: #000000;">Periodo de Contrato: %s</li>
                    <li style="color: #000000;">Fecha de Inicio: %s</li>
                    <li style="color: #000000;">Agentes: %s</li>
                    <li style="color: #000000;">Add-ons: %s</li>
                </ul>
                
                <h3 style="color: #000000;">Conversaciones Contratadas</h3>
                <ul>
                    <li style="color: #000000;">Volumen Mensual: %s</li>
                    <li style="color: #000000;">Canales: %s</li>
                </ul>
                
                <h3 style="color: #FF5722;">Información del Pago</h3>
                <ul>
                    <li style="color: #000000;">Fecha de Confirmación: %s</li>
                    <li style="color: #000000;">Concepto: Setup Fee</li>
                    <li style="color: #000000;">Valor: %s %s</li>
                    <li style="color: #000000;">Estado: PAGADO</li>
                </ul>
                
                <h3 style="color: #FF5722;">Próximos Pasos</h3>
                <ol>
                    <li style="color: #000000;">Por favor, inicien el proceso de onboarding según nuestro protocolo estándar</li>
                    <li style="color: #000000;">Programen la reunión inicial de kick-off con el cliente</li>
                    <li style="color: #000000;">Configuren los accesos necesarios en nuestras plataformas</li>
                    <li style="color: #000000;">Preparen el ambiente según el plan contratado y volumen de conversaciones</li>
                </ol>
                
                <p style="color: #FF5722;">Por favor, confirmen la recepción de este correo y el inicio del proceso de onboarding.</p>
                <p style="color: #FF5722;">Quedo a disposición para cualquier duda o consulta adicional.</p>
                
                <p style="color: #000000;">Saludos cordiales,<br>
                Tu Servicio<br>
                [Tu departamento]</p>
            </body>
            </html>
            """.formatted(
                event.getCompanyName(),
                event.getBmId(),
                event.getCommercialName(),
                event.getPhone(),
                event.getEmail(),
                event.getWebsite(),
                event.getAddress(),
                event.getVertical(),
                event.getBusinessDescription(),
                event.getPlanName(),
                event.getPlanValue(),
                event.getCurrency(),
                event.getContractPeriod(),
                event.getStartDate(),
                event.getAgents(),
                event.getAddons(),
                event.getMonthlyVolume(),
                event.getChannels(),
                event.getPaymentDate(),
                event.getSetupFee(),
                event.getCurrency()
        );
    }

    private String buildCustomerEmailBody(PaymentSuccessEvent event) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="color: #000000;">
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
                <h2 style="color: #000000;">Olá!</h2>

                <p style="color: #000000;">Seja bem-vindo à família CM Mobile!</p>
                
                <p style="color: #000000;">Estamos muito felizes por você ter escolhido se juntar a nós. Sua compra do Broadcaster Bot foi 
                processada com sucesso e agora você faz parte da nossa comunidade de empresas inovadoras.</p>
                
                <h3 style="color: #000000;">Detalhes do Seu Plano contratado</h3>
                <ul>
                    <li style="color: #000000;">Nível do Plano: %s</li>
                    <li style="color: #000000;">Período de Contratação: %s</li>
                    <li style="color: #000000;">Agentes: %s</li>
                    <li style="color: #000000;">Conversas Mensais: %s</li>
                    <li style="color: #000000;">Canais Atribuídos: %s</li>
                    <li style="color: #000000;">Funcionalidades Adicionais: %s</li>
                </ul>
                
                <h3 style="color: #000000;">Vigência</h3>
                <ul>
                    <li style="color: #000000;">Data de Início: %s</li>
                    <li style="color: #000000;">Data de Término: %s</li>
                </ul>
                
                <h3 style="color: #000000;">Investimento</h3>
                <ul>
                    <li style="color: #000000;">Valor: %s %s</li>
                    <li style="color: #000000;">Setup Fee: %s %s PAGO</li>
                </ul>
                
                <h3 style="color: #000000;">Estamos comprometidos em ajudar você a:</h3>
                <ul>
                    <li style="color: #000000;">Otimizar sua estratégia de retenção de clientes</li>
                    <li style="color: #000000;">Reduzir custos operacionais</li>
                    <li style="color: #000000;">Potencializar suas vendas</li>
                    <li style="color: #000000;">Melhorar a experiência dos seus usuários</li>
                </ul>
                
                <p style="color: #000000;">Ótimas notícias! Seu processo de onboarding já está em andamento. Nos próximos dias, nossa 
                equipe de operações entrará em contato para:</p>
                <ul>
                    <li style="color: #000000;">Dar continuidade ao processo de ativação da sua conta</li>
                    <li style="color: #000000;">Realizar as configurações necessárias</li>
                    <li style="color: #000000;">Garantir que você aproveite ao máximo todas as funcionalidades</li>
                    <li style="color: #000000;">Capacitar sua equipe no uso da plataforma</li>
                </ul>
                
                <h3 style="color: #FF5722;">Precisa de ajuda? Estamos aqui para atender você!</h3>
                <ul>
                    <li style="color: #000000;">E-mail de suporte: %s</li>
                    <li style="color: #000000;">Visite nosso site: %s</li>
                    <li style="color: #000000;">Horários de atendimento: %s</li>
                </ul>
                
                <p style="color: #000000;">Como Parceiro Oficial do WhatsApp Business, garantimos que você terá acesso ao que há de 
                mais recente em tecnologia de mensageria empresarial e às melhores práticas do mercado.</p>
                
                <p style="color: #000000;">Obrigado por confiar em nós para fazer seu negócio crescer!</p>
                
                <p style="color: #FF5722;">Atenciosamente,<br>
                A Equipe CM</p>
            </body>
            </html>
            """.formatted(
                event.getPlanName(),
                event.getContractPeriod(),
                event.getAgents(),
                event.getMonthlyVolume(),
                event.getChannels(),
                event.getAddons(),
                event.getStartDate(),
                event.getEndDate(),
                event.getPlanValue(),
                event.getCurrency(),
                event.getSetupFee(),
                event.getCurrency(),
                supportEmail,
                websiteUrl,
                supportHours
        );
    }
}

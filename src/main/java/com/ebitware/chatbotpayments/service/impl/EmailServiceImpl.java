package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ebitware.chatbotpayments.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, false);
    }

    @Override
    public void sendEmail(String to, String subject, String body, String contentType) {
        boolean isHtml = contentType != null && contentType.toLowerCase().contains("html");
        sendEmail(to, subject, body, isHtml);
    }

    private void sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            log.info("Attempting to send {} email to: {}", isHtml ? "HTML" : "plain text", to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
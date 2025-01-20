package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.com.ebitware.stripe.payment.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("Attempting to send email to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
/*
    } catch (MailSendException e) {
        // Check if it's the MailHog specific error
        if (e.getMessage().contains("452 Unable to store message")) {
            // This is expected with MailHog, log as success
            log.info("Email processed by MailHog for: {}", to);
            return; // Don't throw exception as this is expected
        }
        log.error("Failed to send email to: {}", to, e);
        throw new RuntimeException("Failed to send email", e);
    } catch (Exception e) {
        log.error("Failed to send email to: {}", to, e);
        throw new RuntimeException("Failed to send email", e);
    }
*/
}

package mx.com.ebitware.stripe.payment.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}

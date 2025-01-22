package com.ebitware.chatbotpayments.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}

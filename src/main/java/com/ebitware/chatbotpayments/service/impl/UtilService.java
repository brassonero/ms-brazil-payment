package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class UtilService {
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";

    @Value("${app.name:}")
    private String appName ="";

    public String generatePassword() {
        return appName + randomString(8);
    }

    public String randomString(int length) {
        return randomString(length, "both");
    }

    public String randomString(int length, String type) {
        String charset = LOWERCASE + UPPERCASE + NUMBERS;

        if ("letters".equals(type)) {
            charset = LOWERCASE + UPPERCASE;
        } else if ("numbers".equals(type)) {
            charset = NUMBERS;
        }

        StringBuilder text = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            text.append(charset.charAt(random.nextInt(charset.length())));
        }

        return text.toString();
    }
}

package com.ebitware.chatbotpayments.model;

import lombok.Data;

@Data
public class AuthDTO {
    private Long expires;
    private String key;
}

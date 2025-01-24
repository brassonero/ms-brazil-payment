package com.ebitware.chatbotpayments.model;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AccessDTO {
    private Long id;
    private String name;
    private String keyName;
    private ZonedDateTime createdAt;
}

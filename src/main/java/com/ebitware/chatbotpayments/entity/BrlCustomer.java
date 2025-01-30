package com.ebitware.chatbotpayments.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrlCustomer {
    private Long id;
    private String stripeCustomerId;
    private String email;
    private String name;
    private String defaultSource;
    private boolean active;
    private JsonNode metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

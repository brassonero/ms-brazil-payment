package com.ebitware.chatbotpayments.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrlSubscription {
    private String id;
    private String customerId;
    private String status;
    private String priceId;
    private String currency;
    private String paymentMethodId;
    private Map<String, String> metadata;
    private Instant createdAt;
    private Instant updatedAt;
}

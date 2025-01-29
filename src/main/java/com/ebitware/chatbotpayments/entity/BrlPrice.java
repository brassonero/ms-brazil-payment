package com.ebitware.chatbotpayments.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrlPrice {
    private Long id;
    private String stripePriceId;
    private Long productId;
    private String stripeProductId;
    private Long unitAmount;
    private String currency;
    private String interval;
    private boolean active;
    private JsonNode metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

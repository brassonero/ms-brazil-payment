package com.ebitware.chatbotpayments.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrlSubscription {
    private Long id;
    private String stripeSubscriptionId;
    private Long customerId;
    private String stripeCustomerId;
    private Long priceId;
    private String stripePriceId;
    private String status;
    private OffsetDateTime currentPeriodStart;
    private OffsetDateTime currentPeriodEnd;
    private boolean cancelAtPeriodEnd;
    private JsonNode metadata;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

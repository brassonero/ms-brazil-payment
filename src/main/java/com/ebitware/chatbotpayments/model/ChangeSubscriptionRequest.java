package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeSubscriptionRequest {
    @NotNull(message = "New price ID is required")
    private String newPriceId;

    private boolean prorationBehavior = true;
}

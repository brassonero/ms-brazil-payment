package com.ebitware.chatbotpayments.entity;

import lombok.Getter;

@Getter
public enum SubscriptionStatus {
    ACTIVE("active"),
    CANCELED("canceled"),
    INCOMPLETE("incomplete"),
    INCOMPLETE_EXPIRED("incomplete_expired"),
    PAST_DUE("past_due"),
    TRIALING("trialing"),
    UNPAID("unpaid");

    private final String status;

    SubscriptionStatus(String status) {
        this.status = status;
    }

    public static SubscriptionStatus fromString(String text) {
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            if (status.status.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}

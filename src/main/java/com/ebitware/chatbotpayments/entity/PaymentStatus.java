package com.ebitware.chatbotpayments.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("pending"),
    SUCCEEDED("succeeded"),
    FAILED("failed"),
    REQUIRES_ACTION("requires_action"),
    REQUIRES_CONFIRMATION("requires_confirmation"),
    REQUIRES_PAYMENT_METHOD("requires_payment_method"),
    CANCELED("canceled");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public static PaymentStatus fromString(String text) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.status.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}

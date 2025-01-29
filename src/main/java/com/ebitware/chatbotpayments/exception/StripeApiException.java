package com.ebitware.chatbotpayments.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StripeApiException extends RuntimeException {
    private final HttpStatus status;
    private final StripeErrorResponse errorResponse;

    public StripeApiException(String message, HttpStatus status, StripeErrorResponse errorResponse) {
        super(message);
        this.status = status;
        this.errorResponse = errorResponse;
    }
}

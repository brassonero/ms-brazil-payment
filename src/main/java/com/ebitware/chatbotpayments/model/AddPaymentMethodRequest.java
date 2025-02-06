package com.ebitware.chatbotpayments.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AddPaymentMethodRequest {
    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotBlank(message = "Expiry month is required")
    private String expiryMonth;

    @NotBlank(message = "Expiry year is required")
    private String expiryYear;

    @NotBlank(message = "CVC is required")
    private String cvc;

    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;
}

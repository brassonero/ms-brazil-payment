package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfoDTO {
    private String customerId;
    private String paymentIntentId;
    private String subscriptionId;
    private boolean subscriptionActive;
    private String invoiceEmail;
    private String logoUrl;
}

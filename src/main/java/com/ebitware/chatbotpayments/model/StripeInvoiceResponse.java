package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeInvoiceResponse {
    private String invoiceId;
    private String pdfUrl;
}

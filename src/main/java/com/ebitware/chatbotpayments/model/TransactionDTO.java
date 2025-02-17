package com.ebitware.chatbotpayments.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionDTO {
    private String data;           // DATE
    private String numeroDoRecibo; // RECEIPT NUMBER
    private String valorTotal;     // TOTAL VALUE
    private String receiptUrl;
}

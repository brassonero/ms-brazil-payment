package com.ebitware.chatbotpayments.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PricesDTO {
    private String templateType;
    private double cost;
    private boolean free;
}

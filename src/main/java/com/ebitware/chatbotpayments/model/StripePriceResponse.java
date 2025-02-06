package com.ebitware.chatbotpayments.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripePriceResponse {
    private String id;
    private String object;
    private boolean active;
    private String billingScheme;
    private Long created;
    private String currency;
    private JsonNode customUnitAmount;
    private boolean livemode;
    private String lookupKey;
    private Map<String, String> metadata;
    private String nickname;
    private String product;
    private JsonNode recurring;
    private String taxBehavior;
    private String tiersMode;
    private JsonNode transformQuantity;
    private String type;
    private Long unitAmount;
    private String unitAmountDecimal;
}

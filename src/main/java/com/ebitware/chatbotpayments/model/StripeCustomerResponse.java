package com.ebitware.chatbotpayments.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StripeCustomerResponse {
    private String id;
    private String object;
    private String email;
    private String name;
    private String defaultSource;
    private boolean delinquent;
    private Map<String, String> metadata;
}

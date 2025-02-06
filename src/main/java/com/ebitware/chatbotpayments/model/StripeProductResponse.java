package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeProductResponse {
    private String id;
    private String object;
    private boolean active;
    private List<String> attributes;
    private Long created;
    private String defaultPrice;
    private String description;
    private List<String> images;
    private boolean livemode;
    private List<String> marketingFeatures;
    private Map<String, String> metadata;
    private String name;
    private String packageDimensions;
    private Boolean shippable;
    private String statementDescriptor;
    private String taxCode;
    private String type;
    private String unitLabel;
    private Long updated;
    private String url;
}

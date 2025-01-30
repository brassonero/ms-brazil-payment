package com.ebitware.chatbotpayments.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StripeSubscriptionResponse {
    private String id;
    private String object;
    private String status;
    private String customer;
    private String currency;

    @JsonProperty("collection_method")
    private String collectionMethod;

    @JsonProperty("current_period_start")
    private Long currentPeriodStart;

    @JsonProperty("current_period_end")
    private Long currentPeriodEnd;

    @JsonProperty("cancel_at_period_end")
    private Boolean cancelAtPeriodEnd;

    @JsonProperty("canceled_at")
    private Long canceledAt;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("billing_cycle_anchor")
    private Long billingCycleAnchor;

    @JsonProperty("start_date")
    private Long startDate;

    @JsonProperty("latest_invoice")
    private String latestInvoice;

    private Boolean livemode;
    private Map<String, String> metadata;
    private Items items;

    @JsonProperty("default_payment_method")
    private String defaultPaymentMethod;

    @JsonProperty("default_source")
    private String defaultSource;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private String object;
        private List<SubscriptionItem> data;

        @JsonProperty("has_more")
        private Boolean hasMore;

        @JsonProperty("total_count")
        private Integer totalCount;

        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriptionItem {
        private String id;
        private String object;
        private Price price;
        private String subscription;
        private Integer quantity;
        private Map<String, String> metadata;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        private String id;
        private String object;
        private Boolean active;
        private String currency;
        private String product;

        @JsonProperty("unit_amount")
        private Long unitAmount;

        private Recurring recurring;
        private Map<String, String> metadata;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recurring {
        private String interval;

        @JsonProperty("interval_count")
        private Integer intervalCount;

        @JsonProperty("trial_period_days")
        private Integer trialPeriodDays;

        @JsonProperty("usage_type")
        private String usageType;
    }
}

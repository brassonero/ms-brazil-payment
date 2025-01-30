package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlSubscription;
import com.ebitware.chatbotpayments.exception.SubscriptionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Map;


@Repository
public class BrlSubscriptionRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BrlSubscriptionRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public BrlSubscription save(BrlSubscription subscription) {
        String sql = """
            INSERT INTO chatbot.brl_subscriptions 
            (stripe_subscription_id, customer_id, stripe_customer_id, price_id, 
             stripe_price_id, status, current_period_start, current_period_end,
             cancel_at_period_end, metadata)
            VALUES (:stripeSubscriptionId, :customerId, :stripeCustomerId, :priceId,
                    :stripePriceId, :status, :currentPeriodStart, :currentPeriodEnd,
                    :cancelAtPeriodEnd, CAST(:metadata AS jsonb))
            RETURNING id, created_at, updated_at
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("stripeSubscriptionId", subscription.getStripeSubscriptionId())
                .addValue("customerId", subscription.getCustomerId())
                .addValue("stripeCustomerId", subscription.getStripeCustomerId())
                .addValue("priceId", subscription.getPriceId())
                .addValue("stripePriceId", subscription.getStripePriceId())
                .addValue("status", subscription.getStatus())
                .addValue("currentPeriodStart", Timestamp.from(subscription.getCurrentPeriodStart().toInstant()))
                .addValue("currentPeriodEnd", Timestamp.from(subscription.getCurrentPeriodEnd().toInstant()))
                .addValue("cancelAtPeriodEnd", subscription.isCancelAtPeriodEnd())
                .addValue("metadata", convertMetadataToString(subscription.getMetadata()));

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, params);

            subscription.setId((Long) result.get("id"));
            Timestamp createdAt = (Timestamp) result.get("created_at");
            Timestamp updatedAt = (Timestamp) result.get("updated_at");

            subscription.setCreatedAt(createdAt.toInstant().atOffset(ZoneOffset.UTC));
            subscription.setUpdatedAt(updatedAt.toInstant().atOffset(ZoneOffset.UTC));

            return subscription;
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("unique_stripe_subscription_id")) {
                throw new SubscriptionException("Subscription with this Stripe ID already exists");
            }
            throw e;
        }
    }

    private String convertMetadataToString(JsonNode metadata) {
        try {
            return metadata != null ? new ObjectMapper().writeValueAsString(metadata) : null;
        } catch (JsonProcessingException e) {
            throw new SubscriptionException("Error converting metadata to JSON", e);
        }
    }
}
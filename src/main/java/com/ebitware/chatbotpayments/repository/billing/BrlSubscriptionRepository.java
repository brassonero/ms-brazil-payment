package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlSubscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class BrlSubscriptionRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public BrlSubscriptionRepository(@Qualifier("billingDataSource") DataSource dataSource, ObjectMapper objectMapper) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
    }

    public void save(String subscriptionId, String customerId, String status,
                     String priceId, String currency, String paymentMethodId,
                     Map<String, String> metadata) {
        String sql = """
                    INSERT INTO chatbot.brl_subscriptions (id, customer_id, status, price_id, 
                                             currency, payment_method_id, metadata, created_at, updated_at)
                    VALUES (:id, :customerId, :status, :priceId, 
                            :currency, :paymentMethodId, CAST(:metadata AS jsonb), NOW(), NOW())
                    ON CONFLICT (id) DO UPDATE 
                    SET status = :status,
                        metadata = CAST(:metadata AS jsonb),
                        updated_at = NOW()
                """;

        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", subscriptionId)
                    .addValue("customerId", customerId)
                    .addValue("status", status)
                    .addValue("priceId", priceId)
                    .addValue("currency", currency)
                    .addValue("paymentMethodId", paymentMethodId)
                    .addValue("metadata", objectMapper.writeValueAsString(metadata));

            jdbcTemplate.update(sql, params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing metadata", e);
        }
    }

    public void updateSubscription(String subscriptionId, String status, String priceId,
                                   String paymentMethodId, Map<String, String> metadata) {
        String sql = """
                    UPDATE chatbot.brl_subscriptions 
                    SET status = :status,
                        price_id = :priceId,
                        payment_method_id = :paymentMethodId,
                        metadata = CAST(:metadata AS jsonb),
                        updated_at = NOW()
                    WHERE id = :id
                """;

        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", subscriptionId)
                    .addValue("status", status)
                    .addValue("priceId", priceId)
                    .addValue("paymentMethodId", paymentMethodId)
                    .addValue("metadata", objectMapper.writeValueAsString(metadata));

            int updatedRows = jdbcTemplate.update(sql, params);
            if (updatedRows == 0) {
                log.warn("No subscription found to update with ID: {}", subscriptionId);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing metadata", e);
        }
    }

    public Optional<BrlSubscription> findById(String subscriptionId) {
        String sql = """
            SELECT id, customer_id, status, price_id, currency, payment_method_id, metadata, 
                   created_at, updated_at
            FROM chatbot.brl_subscriptions
            WHERE id = :id
        """;

        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", subscriptionId);

            return Optional.ofNullable(jdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    BrlSubscription subscription = new BrlSubscription();
                    subscription.setId(rs.getString("id"));
                    subscription.setCustomerId(rs.getString("customer_id"));
                    subscription.setStatus(rs.getString("status"));
                    subscription.setPriceId(rs.getString("price_id"));
                    subscription.setCurrency(rs.getString("currency"));
                    subscription.setPaymentMethodId(rs.getString("payment_method_id"));

                    String metadataJson = rs.getString("metadata");
                    if (metadataJson != null) {
                        try {
                            subscription.setMetadata(
                                    objectMapper.readValue(metadataJson,
                                            new TypeReference<Map<String, String>>() {})
                            );
                        } catch (JsonProcessingException e) {
                            log.error("Error parsing metadata JSON", e);
                            subscription.setMetadata(new HashMap<>());
                        }
                    }

                    subscription.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    subscription.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                    return subscription;
                }
                return null;
            }));
        } catch (Exception e) {
            log.error("Error finding subscription by ID", e);
            throw new RuntimeException("Error retrieving subscription", e);
        }
    }

    public Optional<Map<String, Object>> findPlanDetails(String customerId) {
        String sql = """
        SELECT s.id, 
               s.status as subscription_status, 
               s.metadata as subscription_metadata,
               s.created_at as start_date, 
               p.name as plan_name,
               s.price_id,
               pr.interval_type,
               pr.unit_amount,
               pr.currency
        FROM chatbot.brl_subscriptions s
        JOIN chatbot.brl_prices pr ON s.price_id = pr.stripe_price_id
        JOIN chatbot.brl_products p ON pr.stripe_product_id = p.stripe_product_id
        WHERE s.customer_id = :customerId
        AND s.status IN ('active', 'trialing')
        ORDER BY s.created_at DESC
        LIMIT 1
    """;

        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("customerId", customerId);

            return Optional.ofNullable(jdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("subscription_status", rs.getString("subscription_status"));
                    result.put("plan_name", rs.getString("plan_name"));
                    result.put("interval_type", rs.getString("interval_type"));
                    result.put("start_date", rs.getTimestamp("start_date"));
                    result.put("currency", rs.getString("currency"));
                    result.put("unit_amount", rs.getDouble("unit_amount"));

                    // Parse metadata
                    String metadataJson = rs.getString("subscription_metadata");
                    if (metadataJson != null) {
                        try {
                            Map<String, String> metadata = objectMapper.readValue(metadataJson,
                                    new TypeReference<Map<String, String>>() {});
                            result.put("extra_agent", metadata.getOrDefault("extra_agent", "No"));
                        } catch (JsonProcessingException e) {
                            log.error("Error parsing metadata JSON", e);
                            result.put("extra_agent", "No");
                        }
                    } else {
                        result.put("extra_agent", "No");
                    }

                    return result;
                }
                return null;
            }));
        } catch (Exception e) {
            log.error("Error finding plan details for customer: {}", customerId, e);
            throw new RuntimeException("Error retrieving plan details", e);
        }
    }
}

package com.ebitware.chatbotpayments.repository.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;

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
}

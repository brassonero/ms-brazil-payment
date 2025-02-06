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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class BrlPaymentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public BrlPaymentRepository(@Qualifier("billingDataSource") DataSource dataSource, ObjectMapper objectMapper) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
    }

    public void save(String paymentIntentId, String customerId, String paymentMethodId,
                     String paymentType, String currency, BigDecimal amount,
                     String status, Map<String, String> metadata) {
        String sql = """
                    INSERT INTO chatbot.brl_payments (id, customer_id, payment_method_id, payment_type, 
                                        currency, amount, status, metadata, created_at, updated_at)
                    VALUES (:id, :customerId, :paymentMethodId, :paymentType, 
                            :currency, :amount, :status, CAST(:metadata AS jsonb), NOW(), NOW())
                    ON CONFLICT (id) DO UPDATE 
                    SET status = :status,
                        metadata = CAST(:metadata AS jsonb),
                        updated_at = NOW()
                """;

        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", paymentIntentId)
                    .addValue("customerId", customerId)
                    .addValue("paymentMethodId", paymentMethodId)
                    .addValue("paymentType", paymentType)
                    .addValue("currency", currency)
                    .addValue("amount", amount)
                    .addValue("status", status)
                    .addValue("metadata", objectMapper.writeValueAsString(metadata));

            jdbcTemplate.update(sql, params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing metadata", e);
        }
    }

    public List<Map<String, Object>> findPaymentReceipts(String customerId, int page, int size) {
        int offset = page * size;

        StringBuilder sql = new StringBuilder("""
        SELECT p.id, p.amount, p.created_at, p.status
        FROM chatbot.brl_payments p
        WHERE p.status = 'succeeded'
    """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", size)
                .addValue("offset", offset);

        if (customerId != null && !customerId.trim().isEmpty()) {
            sql.append(" AND p.customer_id = :customerId");
            params.addValue("customerId", customerId);
        }

        sql.append("""
        ORDER BY p.created_at DESC
        LIMIT :limit OFFSET :offset
    """);

        return jdbcTemplate.queryForList(sql.toString(), params);
    }
}

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
import java.util.HashMap;
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

    public List<Map<String, Object>> findByCustomerId(String customerId, int page, int size) {
        int offset = page * size;
        String sql = """
            SELECT p.*, c.name as customer_name, c.document, c.document_type 
            FROM chatbot.brl_payments p
            LEFT JOIN chatbot.brl_customers c ON p.customer_id = c.id
            WHERE p.customer_id = :customerId
            ORDER BY p.created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("limit", size)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> payment = new HashMap<>();
            payment.put("id", rs.getString("id"));
            payment.put("amount", rs.getBigDecimal("amount"));
            payment.put("currency", rs.getString("currency"));
            payment.put("status", rs.getString("status"));
            payment.put("payment_type", rs.getString("payment_type"));
            payment.put("created_at", rs.getTimestamp("created_at").toInstant());
            payment.put("customer_name", rs.getString("customer_name"));
            payment.put("document", rs.getString("document"));
            payment.put("document_type", rs.getString("document_type"));

            // Parse metadata JSON if present
            String metadataJson = rs.getString("metadata");
            if (metadataJson != null) {
                try {
                    payment.put("metadata", objectMapper.readValue(metadataJson, Map.class));
                } catch (JsonProcessingException e) {
                    log.error("Error parsing metadata for payment {}: {}", rs.getString("id"), e.getMessage());
                }
            }

            return payment;
        });
    }

    public long countByCustomerId(String customerId) {
        String sql = "SELECT COUNT(*) FROM chatbot.brl_payments WHERE customer_id = :customerId";

        return jdbcTemplate.queryForObject(sql,
                new MapSqlParameterSource("customerId", customerId),
                Long.class
        );
    }
}

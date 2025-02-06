package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.CustomerInfoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class CustomerInfoRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CustomerInfoRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Optional<CustomerInfoDTO> findCustomerInfoByPersonId(Integer personId) {
        String sql = """
        SELECT 
            COALESCE(c.id, '') as customer_id,
            COALESCE(p.id, '') as payment_intent_id,
            COALESCE(s.id, '') as subscription_id,
            COALESCE(s.status, '') as subscription_status,
            COALESCE(i.billing_email, '') as email,
            COALESCE(fs.logo_url, '') as logo_url
        FROM chatbot.brl_form_submission fs
        LEFT JOIN chatbot.brl_customers c ON c.person_id = fs.person_id
        LEFT JOIN (
            SELECT DISTINCT ON (customer_id) *
            FROM chatbot.brl_payments
            ORDER BY customer_id, created_at DESC
        ) p ON p.customer_id = c.id
        LEFT JOIN (
            SELECT DISTINCT ON (customer_id) *
            FROM chatbot.brl_subscriptions
            ORDER BY customer_id, created_at DESC
        ) s ON s.customer_id = c.id
        LEFT JOIN (
            SELECT person_id, billing_email
            FROM chatbot.brl_invoice_info
            WHERE person_id = :personId
            LIMIT 1
        ) i ON i.person_id = fs.person_id
        WHERE fs.person_id = :personId
    """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("personId", personId);

            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params,
                    (rs, rowNum) -> new CustomerInfoDTO(
                            rs.getString("customer_id"),
                            rs.getString("payment_intent_id"),
                            rs.getString("subscription_id"),
                            "active".equals(rs.getString("subscription_status")),
                            rs.getString("email"),
                            rs.getString("logo_url")
                    )));
        } catch (EmptyResultDataAccessException e) {
            return Optional.of(new CustomerInfoDTO("", "", "", false, "", ""));
        }
    }
}

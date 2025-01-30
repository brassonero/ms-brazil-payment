package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.exception.CustomerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static com.ebitware.chatbotpayments.constants.SqlConstants.FIND_CUSTOMERS_ID;
import static com.ebitware.chatbotpayments.constants.SqlConstants.INSERT_CUSTOMER;

@Repository
public class BrlCustomerRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BrlCustomerRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public BrlCustomer save(BrlCustomer customer) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("stripeCustomerId", customer.getStripeCustomerId())
                .addValue("email", customer.getEmail())
                .addValue("name", customer.getName())
                .addValue("defaultSource", customer.getDefaultSource())
                .addValue("active", customer.isActive())
                .addValue("metadata", convertMetadataToString(customer.getMetadata()));

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(INSERT_CUSTOMER, params);

            customer.setId((Long) result.get("id"));
            Timestamp createdAt = (Timestamp) result.get("created_at");
            Timestamp updatedAt = (Timestamp) result.get("updated_at");

            customer.setCreatedAt(createdAt.toInstant().atOffset(ZoneOffset.UTC));
            customer.setUpdatedAt(updatedAt.toInstant().atOffset(ZoneOffset.UTC));

            return customer;
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("unique_stripe_customer_id")) {
                throw new CustomerException("Customer with this Stripe ID already exists");
            } else if (e.getMessage().contains("unique_customer_email")) {
                throw new CustomerException("Customer with this email already exists");
            }
            throw e;
        }
    }

    private BrlCustomer mapRowToCustomer(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata;
        try {
            String metadataStr = rs.getString("metadata");
            metadata = metadataStr != null ? mapper.readTree(metadataStr) : null;
        } catch (JsonProcessingException e) {
            throw new CustomerException("Error parsing metadata JSON", e);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new BrlCustomer(
                rs.getLong("id"),
                rs.getString("stripe_customer_id"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("default_source"),
                rs.getBoolean("active"),
                metadata,
                createdAt != null ? createdAt.toInstant().atOffset(ZoneOffset.UTC) : null,
                updatedAt != null ? updatedAt.toInstant().atOffset(ZoneOffset.UTC) : null
        );
    }

    public Optional<BrlCustomer> findById(Long id) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_CUSTOMERS_ID, params, this::mapRowToCustomer));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private String convertMetadataToString(JsonNode metadata) {
        try {
            return metadata != null ? new ObjectMapper().writeValueAsString(metadata) : null;
        } catch (JsonProcessingException e) {
            throw new CustomerException("Error converting metadata to JSON", e);
        }
    }
}

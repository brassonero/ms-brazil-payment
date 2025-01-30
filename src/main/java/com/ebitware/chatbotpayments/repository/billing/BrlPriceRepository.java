package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.exception.PriceException;
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

import static com.ebitware.chatbotpayments.constants.SqlConstants.FIND_PRICE_BY_ID;
import static com.ebitware.chatbotpayments.constants.SqlConstants.INSERT_PRICE;

@Repository
public class BrlPriceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BrlPriceRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public BrlPrice save(BrlPrice price) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("stripePriceId", price.getStripePriceId())
                .addValue("productId", price.getProductId())
                .addValue("stripeProductId", price.getStripeProductId())
                .addValue("unitAmount", price.getUnitAmount())
                .addValue("currency", price.getCurrency())
                .addValue("interval", price.getInterval())
                .addValue("active", price.isActive())
                .addValue("metadata", convertMetadataToString(price.getMetadata()));

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(INSERT_PRICE, params);

            price.setId((Long) result.get("id"));

            // Convert Timestamp to OffsetDateTime
            Timestamp createdAt = (Timestamp) result.get("created_at");
            Timestamp updatedAt = (Timestamp) result.get("updated_at");

            price.setCreatedAt(createdAt.toInstant().atOffset(ZoneOffset.UTC));
            price.setUpdatedAt(updatedAt.toInstant().atOffset(ZoneOffset.UTC));

            return price;
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("unique_stripe_price_id")) {
                throw new PriceException("Price with this Stripe ID already exists");
            }
            throw e;
        }
    }

    private String convertMetadataToString(JsonNode metadata) {
        try {
            return metadata != null ? new ObjectMapper().writeValueAsString(metadata) : null;
        } catch (JsonProcessingException e) {
            throw new PriceException("Error converting metadata to JSON", e);
        }
    }

    private BrlPrice mapRowToBrlPrice(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata;
        try {
            String metadataStr = rs.getString("metadata");
            metadata = metadataStr != null ? mapper.readTree(metadataStr) : null;
        } catch (JsonProcessingException e) {
            throw new PriceException("Error parsing metadata JSON", e);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new BrlPrice(
                rs.getLong("id"),
                rs.getString("stripe_price_id"),
                rs.getLong("product_id"),
                rs.getString("stripe_product_id"),
                rs.getDouble("unit_amount"),  // Changed from getLong to getDouble
                rs.getString("currency"),
                rs.getString("interval_type"),
                rs.getBoolean("active"),
                metadata,
                createdAt != null ? createdAt.toInstant().atOffset(ZoneOffset.UTC) : null,
                updatedAt != null ? updatedAt.toInstant().atOffset(ZoneOffset.UTC) : null
        );
    }

    public Optional<BrlPrice> findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_PRICE_BY_ID, params, this::mapRowToBrlPrice));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

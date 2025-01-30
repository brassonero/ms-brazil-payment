package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.ProductException;
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

import static com.ebitware.chatbotpayments.constants.SqlConstants.FIND_PRODUCTS_BY_ID;
import static com.ebitware.chatbotpayments.constants.SqlConstants.INSERT_PRODUCT;

@Repository
public class BrlProductRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BrlProductRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public BrlProduct save(BrlProduct product) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("stripeProductId", product.getStripeProductId())
                .addValue("name", product.getName())
                .addValue("description", product.getDescription())
                .addValue("active", product.isActive())
                .addValue("metadata", convertMetadataToString(product.getMetadata()));

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(INSERT_PRODUCT, params);

            product.setId((Long) result.get("id"));

            // Convert Timestamp to OffsetDateTime
            Timestamp createdAt = (Timestamp) result.get("created_at");
            Timestamp updatedAt = (Timestamp) result.get("updated_at");

            product.setCreatedAt(createdAt.toInstant().atOffset(ZoneOffset.UTC));
            product.setUpdatedAt(updatedAt.toInstant().atOffset(ZoneOffset.UTC));

            return product;
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("unique_stripe_product_id")) {
                throw new ProductException("Product with this Stripe ID already exists");
            } else if (e.getMessage().contains("unique_product_name")) {
                throw new ProductException("Product with this name already exists");
            }
            throw e;
        }
    }

    private String convertMetadataToString(JsonNode metadata) {
        try {
            return metadata != null ? new ObjectMapper().writeValueAsString(metadata) : null;
        } catch (JsonProcessingException e) {
            throw new ProductException("Error converting metadata to JSON", e);
        }
    }

    private BrlProduct mapRowToBrlProduct(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata;
        try {
            String metadataStr = rs.getString("metadata");
            metadata = metadataStr != null ? mapper.readTree(metadataStr) : null;
        } catch (JsonProcessingException e) {
            throw new ProductException("Error parsing metadata JSON", e);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new BrlProduct(
                rs.getLong("id"),
                rs.getString("stripe_product_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("active"),
                metadata,
                createdAt != null ? createdAt.toInstant().atOffset(ZoneOffset.UTC) : null,
                updatedAt != null ? updatedAt.toInstant().atOffset(ZoneOffset.UTC) : null
        );
    }

    public Optional<BrlProduct> findById(Long id) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_PRODUCTS_BY_ID, params, this::mapRowToBrlProduct));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

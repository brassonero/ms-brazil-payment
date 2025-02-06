package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class BrlCustomerRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public BrlCustomerRepository(@Qualifier("billingDataSource") DataSource dataSource, ObjectMapper objectMapper) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
    }

    private class CustomerRowMapper implements RowMapper<BrlCustomer> {
        @Override
        public BrlCustomer mapRow(ResultSet rs, int rowNum) throws SQLException {
            BrlCustomer brlCustomer = new BrlCustomer();
            brlCustomer.setId(rs.getString("id"));
            brlCustomer.setDocument(rs.getString("document"));
            brlCustomer.setDocumentType(rs.getString("document_type"));
            brlCustomer.setName(rs.getString("name"));

            String metadataJson = rs.getString("metadata");
            if (metadataJson != null) {
                try {
                    brlCustomer.setMetadata(objectMapper.readValue(metadataJson,
                            new TypeReference<Map<String, String>>() {
                            }));
                } catch (JsonProcessingException e) {
                    log.error("Error parsing metadata JSON for customer {}: {}",
                            rs.getString("id"), e.getMessage());
                    brlCustomer.setMetadata(new HashMap<>());
                }
            }

            brlCustomer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            brlCustomer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            return brlCustomer;
        }
    }

    public void save(String customerId, String document, String documentType,
                     String name, Map<String, String> metadata, Integer personId) {
        log.info("Attempting to save customer to database - ID: {}, Document: {}, Type: {}",
                customerId, document, documentType);

        String sql = """
        INSERT INTO chatbot.brl_customers 
        (id, document, document_type, name, metadata, person_id, created_at, updated_at)
        VALUES (:id, :document, :documentType, :name, CAST(:metadata AS jsonb), 
                :personId, NOW(), NOW())
        ON CONFLICT (id) DO UPDATE 
        SET document = :document,
            document_type = :documentType,
            name = :name,
            metadata = CAST(:metadata AS jsonb),
            person_id = :personId,
            updated_at = NOW()
        """;

        try {
            String metadataJson = objectMapper.writeValueAsString(metadata);
            log.debug("Metadata JSON: {}", metadataJson);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", customerId)
                    .addValue("document", document)
                    .addValue("documentType", documentType)
                    .addValue("name", name)
                    .addValue("metadata", metadataJson)
                    .addValue("personId", personId);

            int result = jdbcTemplate.update(sql, params);
            log.info("Database save result: {} rows affected", result);

        } catch (JsonProcessingException e) {
            log.error("Error serializing metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Error serializing metadata", e);
        } catch (Exception e) {
            log.error("Error executing database save: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving customer", e);
        }
    }

    public Optional<BrlCustomer> findById(String customerId) {
        String sql = "SELECT * FROM chatbot.brl_customers WHERE id = :id";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                    new MapSqlParameterSource("id", customerId),
                    new CustomerRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<BrlCustomer> findByDocumentAndDocumentType(String document, String documentType) {
        String sql = """
            SELECT * FROM chatbot.brl_customers 
            WHERE document = :document 
            AND document_type = :documentType
            ORDER BY created_at DESC 
            LIMIT 1
            """;

        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("document", document)
                    .addValue("documentType", documentType);

            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                    params,
                    new CustomerRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

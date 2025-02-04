package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.InvoiceEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InvoiceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InvoiceRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public int save(InvoiceEntity invoiceEntityDTO) {
        String sql = """
        INSERT INTO chatbot.brl_invoice_info (
            fiscal_regime, business_name, id_type, id_number, billing_email, phone, street, state, city, country, postal_code, person_id
        ) VALUES (
            :fiscalRegime, :businessName, :idType, :idNumber, :billingEmail, :phone, :street, :state, :city, :country, :postalCode, :personId
        )
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("fiscalRegime", invoiceEntityDTO.getFiscalRegime());
        params.put("businessName", invoiceEntityDTO.getBusinessName());
        params.put("idType", invoiceEntityDTO.getIdType());
        params.put("idNumber", invoiceEntityDTO.getIdNumber());
        params.put("billingEmail", invoiceEntityDTO.getBillingEmail());
        params.put("phone", invoiceEntityDTO.getPhone());
        params.put("street", invoiceEntityDTO.getStreet());
        params.put("state", invoiceEntityDTO.getState());
        params.put("city", invoiceEntityDTO.getCity());
        params.put("country", invoiceEntityDTO.getCountry());
        params.put("postalCode", invoiceEntityDTO.getPostalCode());
        params.put("personId", invoiceEntityDTO.getPersonId());

        return jdbcTemplate.update(sql, params);
    }

    public int update(String email, InvoiceEntity invoiceEntityDTO) {

        String countSql = "SELECT COUNT(*) FROM chatbot.brl_invoice_info WHERE billing_email = :email";
        Map<String, Object> countParams = new HashMap<>();
        countParams.put("email", email);

        Integer count = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);

        if (count == null || count == 0) {
            throw new IllegalArgumentException("Billing information with the provided email does not exist.");
        }

        String sql = """
        UPDATE chatbot.brl_invoice_info
        SET tax_id = :taxId,
            cfdi_usage = :cfdiUsage,
            fiscal_regime = :fiscalRegime,
            business_name = :businessName,
            street = :street,
            neighborhood = :neighborhood,
            postal_code = :postalCode,
            country = :country,
            state = :state,
            city = :city,
            updated_at = CURRENT_TIMESTAMP
        WHERE billing_email = :email
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("taxId", invoiceEntityDTO.getTaxId());
        params.put("cfdiUsage", invoiceEntityDTO.getCfdiUsage());
        params.put("fiscalRegime", invoiceEntityDTO.getFiscalRegime());
        params.put("businessName", invoiceEntityDTO.getBusinessName());
        params.put("street", invoiceEntityDTO.getStreet());
        params.put("neighborhood", invoiceEntityDTO.getNeighborhood());
        params.put("postalCode", invoiceEntityDTO.getPostalCode());
        params.put("country", invoiceEntityDTO.getCountry());
        params.put("state", invoiceEntityDTO.getState());
        params.put("city", invoiceEntityDTO.getCity());
        params.put("email", email);

        return jdbcTemplate.update(sql, params);
    }

    public Optional<InvoiceEntity> findByEmail(String email) {
        String sql = """
        SELECT * FROM chatbot.brl_invoice_info 
        WHERE billing_email = :email
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("email", email);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
                InvoiceEntity invoiceEntity = new InvoiceEntity();
                invoiceEntity.setTaxId(rs.getString("tax_id"));
                invoiceEntity.setBusinessName(rs.getString("business_name"));
                invoiceEntity.setFiscalRegime(rs.getString("fiscal_regime"));
                invoiceEntity.setCfdiUsage(rs.getString("cfdi_usage"));
                invoiceEntity.setBillingEmail(rs.getString("billing_email"));
                // Set other fields as needed
                return invoiceEntity;
            }));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

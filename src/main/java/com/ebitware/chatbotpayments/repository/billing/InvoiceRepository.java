package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlInvoice;
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

    public int save(BrlInvoice brlInvoiceDTO) {
        String sql = """
        INSERT INTO chatbot.brl_invoice_info (
            fiscal_regime, business_name, id_type, id_number, billing_email, phone, street, state, city, country, postal_code, person_id
        ) VALUES (
            :fiscalRegime, :businessName, :idType, :idNumber, :billingEmail, :phone, :street, :state, :city, :country, :postalCode, :personId
        )
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("fiscalRegime", brlInvoiceDTO.getFiscalRegime());
        params.put("businessName", brlInvoiceDTO.getBusinessName());
        params.put("idType", brlInvoiceDTO.getIdType());
        params.put("idNumber", brlInvoiceDTO.getIdNumber());
        params.put("billingEmail", brlInvoiceDTO.getBillingEmail());
        params.put("phone", brlInvoiceDTO.getPhone());
        params.put("street", brlInvoiceDTO.getStreet());
        params.put("state", brlInvoiceDTO.getState());
        params.put("city", brlInvoiceDTO.getCity());
        params.put("country", brlInvoiceDTO.getCountry());
        params.put("postalCode", brlInvoiceDTO.getPostalCode());
        params.put("personId", brlInvoiceDTO.getPersonId());

        return jdbcTemplate.update(sql, params);
    }

    public int update(String email, BrlInvoice brlInvoiceDTO) {

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
        params.put("taxId", brlInvoiceDTO.getTaxId());
        params.put("cfdiUsage", brlInvoiceDTO.getCfdiUsage());
        params.put("fiscalRegime", brlInvoiceDTO.getFiscalRegime());
        params.put("businessName", brlInvoiceDTO.getBusinessName());
        params.put("street", brlInvoiceDTO.getStreet());
        params.put("neighborhood", brlInvoiceDTO.getNeighborhood());
        params.put("postalCode", brlInvoiceDTO.getPostalCode());
        params.put("country", brlInvoiceDTO.getCountry());
        params.put("state", brlInvoiceDTO.getState());
        params.put("city", brlInvoiceDTO.getCity());
        params.put("email", email);

        return jdbcTemplate.update(sql, params);
    }

    public Optional<BrlInvoice> findByEmail(String email) {
        String sql = """
        SELECT * FROM chatbot.brl_invoice_info 
        WHERE billing_email = :email
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("email", email);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
                BrlInvoice brlInvoice = new BrlInvoice();
                brlInvoice.setTaxId(rs.getString("tax_id"));
                brlInvoice.setBusinessName(rs.getString("business_name"));
                brlInvoice.setFiscalRegime(rs.getString("fiscal_regime"));
                brlInvoice.setCfdiUsage(rs.getString("cfdi_usage"));
                brlInvoice.setBillingEmail(rs.getString("billing_email"));
                // Set other fields as needed
                return brlInvoice;
            }));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

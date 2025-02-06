package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlInvoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class InvoiceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InvoiceRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public int save(BrlInvoice invoice) {
        String sql = """
            INSERT INTO chatbot.brl_invoice_info (
                fiscal_regime, 
                business_name, 
                id_type, 
                id_number, 
                billing_email, 
                phone, 
                street, 
                state, 
                city, 
                country, 
                postal_code,
                tax_id,
                cfdi_usage,
                neighborhood,
                person_id
            ) VALUES (
                :fiscalRegime, 
                :businessName, 
                :idType, 
                :idNumber, 
                :billingEmail, 
                :phone, 
                :street, 
                :state, 
                :city, 
                :country, 
                :postalCode,
                :taxId,
                :cfdiUsage,
                :neighborhood,
                :personId
            )
        """;

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("fiscalRegime", invoice.getFiscalRegime())
                    .addValue("businessName", invoice.getBusinessName())
                    .addValue("idType", invoice.getIdType())
                    .addValue("idNumber", invoice.getIdNumber())
                    .addValue("billingEmail", invoice.getBillingEmail())
                    .addValue("phone", invoice.getPhone())
                    .addValue("street", invoice.getStreet())
                    .addValue("state", invoice.getState())
                    .addValue("city", invoice.getCity())
                    .addValue("country", invoice.getCountry())
                    .addValue("postalCode", invoice.getPostalCode())
                    .addValue("taxId", invoice.getTaxId())
                    .addValue("cfdiUsage", invoice.getCfdiUsage())
                    .addValue("neighborhood", invoice.getNeighborhood())
                    .addValue("personId", invoice.getPersonId());

            log.debug("Executing insert with parameters: {}", params.getValues());

            int rowsAffected = jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

            if (rowsAffected > 0 && keyHolder.getKey() != null) {
                log.debug("Insert successful, generated id: {}", keyHolder.getKey().intValue());
                return keyHolder.getKey().intValue();
            } else {
                log.error("Insert failed, no rows affected or no id generated");
                return -1;
            }
        } catch (Exception e) {
            log.error("Error saving invoice: {}", e.getMessage(), e);
            throw e;
        }
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
        SELECT 
            id, fiscal_regime, business_name, id_type, id_number, 
            billing_email, phone, street, state, city, country, 
            postal_code, tax_id, cfdi_usage, neighborhood, person_id,
            created_at, updated_at
        FROM chatbot.brl_invoice_info 
        WHERE billing_email = :email
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("email", email);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
                BrlInvoice brlInvoice = new BrlInvoice();
                brlInvoice.setId(rs.getInt("id"));
                brlInvoice.setFiscalRegime(rs.getString("fiscal_regime"));
                brlInvoice.setBusinessName(rs.getString("business_name"));
                brlInvoice.setIdType(rs.getString("id_type"));
                brlInvoice.setIdNumber(rs.getString("id_number"));
                brlInvoice.setBillingEmail(rs.getString("billing_email"));
                brlInvoice.setPhone(rs.getString("phone"));
                brlInvoice.setStreet(rs.getString("street"));
                brlInvoice.setState(rs.getString("state"));
                brlInvoice.setCity(rs.getString("city"));
                brlInvoice.setCountry(rs.getString("country"));
                brlInvoice.setPostalCode(rs.getString("postal_code"));
                brlInvoice.setTaxId(rs.getString("tax_id"));
                brlInvoice.setCfdiUsage(rs.getString("cfdi_usage"));
                brlInvoice.setNeighborhood(rs.getString("neighborhood"));
                brlInvoice.setPersonId(rs.getInt("person_id"));
                return brlInvoice;
            }));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

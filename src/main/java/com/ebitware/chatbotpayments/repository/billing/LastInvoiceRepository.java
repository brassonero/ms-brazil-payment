package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.InvoiceDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Repository
public class LastInvoiceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LastInvoiceRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public int save(InvoiceDTO invoiceDTO) {
        String sql = """
            INSERT INTO bcb_invoice_info (
                fiscal_regime, business_name, id_type, id_number, billing_email, phone, street, state, city, country, postal_code
            ) VALUES (
                :fiscalRegime, :businessName, :idType, :idNumber, :billingEmail, :phone, :street, :state, :city, :country, :postalCode
            )
        """;
        Map<String, Object> params = new HashMap<>();
        params.put("fiscalRegime", invoiceDTO.getFiscalRegime());
        params.put("businessName", invoiceDTO.getBusinessName());
        params.put("idType", invoiceDTO.getIdType());
        params.put("idNumber", invoiceDTO.getIdNumber());
        params.put("billingEmail", invoiceDTO.getBillingEmail());
        params.put("phone", invoiceDTO.getPhone());
        params.put("street", invoiceDTO.getStreet());
        params.put("state", invoiceDTO.getState());
        params.put("city", invoiceDTO.getCity());
        params.put("country", invoiceDTO.getCountry());
        params.put("postalCode", invoiceDTO.getPostalCode());

        return jdbcTemplate.update(sql, params);
    }

    public int update(String email, InvoiceDTO invoiceDTO) {
        String sql = """
            UPDATE bcb_invoice_info
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
        params.put("taxId", invoiceDTO.getTaxId());
        params.put("cfdiUsage", invoiceDTO.getCfdiUsage());
        params.put("fiscalRegime", invoiceDTO.getFiscalRegime());
        params.put("businessName", invoiceDTO.getBusinessName());
        params.put("street", invoiceDTO.getStreet());
        params.put("neighborhood", invoiceDTO.getNeighborhood());
        params.put("postalCode", invoiceDTO.getPostalCode());
        params.put("country", invoiceDTO.getCountry());
        params.put("state", invoiceDTO.getState());
        params.put("city", invoiceDTO.getCity());
        params.put("email", email);

        return jdbcTemplate.update(sql, params);
    }
}

package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.InvoiceEntity;
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

    public int save(InvoiceEntity invoiceEntityDTO) {
        String sql = """
            INSERT INTO bcb_invoice_info (
                fiscal_regime, business_name, id_type, id_number, billing_email, phone, street, state, city, country, postal_code
            ) VALUES (
                :fiscalRegime, :businessName, :idType, :idNumber, :billingEmail, :phone, :street, :state, :city, :country, :postalCode
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

        return jdbcTemplate.update(sql, params);
    }

    public int update(String email, InvoiceEntity invoiceEntityDTO) {
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
}

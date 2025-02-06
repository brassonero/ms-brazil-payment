package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.BrlInvoice;
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

    public int save(BrlInvoice brlInvoiceDTO) {
        String sql = """
            INSERT INTO bcb_invoice_info (
                fiscal_regime, business_name, id_type, id_number, billing_email, phone, street, state, city, country, postal_code
            ) VALUES (
                :fiscalRegime, :businessName, :idType, :idNumber, :billingEmail, :phone, :street, :state, :city, :country, :postalCode
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

        return jdbcTemplate.update(sql, params);
    }

    public int update(String email, BrlInvoice brlInvoiceDTO) {
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
}

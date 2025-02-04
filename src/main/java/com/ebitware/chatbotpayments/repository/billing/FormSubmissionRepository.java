package com.ebitware.chatbotpayments.repository.billing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.ebitware.chatbotpayments.model.FormSubmissionRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.ebitware.chatbotpayments.constants.SqlConstants.*;

@Slf4j
@Repository
public class FormSubmissionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public FormSubmissionRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void saveSubmissionForm(FormSubmissionRequest form, String logoUrl) {

        Map<String, Object> params = new HashMap<>();
        params.put("businessName", form.getBusinessName());
        params.put("displayName", form.getDisplayName());
        params.put("corporateEmail", form.getCorporateEmail());
        params.put("website", form.getWebsite());
        params.put("description", form.getDescription());
        params.put("facebookManagerNo", form.getFacebookManagerNo());
        params.put("phone", form.getPhone());
        params.put("address", form.getAddress());
        params.put("vertical", form.getVertical());
        params.put("logoUrl", logoUrl);

        jdbcTemplate.update(INSERT_FORM_SUBMISSION, params);
    }

    public boolean emailExists(String email) {
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);

        Integer count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS_DEPRECATED, params, Integer.class);
        return count != null && count > 0;
    }

    private static final String SAVE_TOKEN =
            "UPDATE chatbot.brl_form_submission " +
                    "SET confirmation_token = :token, " +
                    "token_expiry = :expiry, " +
                    "confirmation_sent_at = CURRENT_TIMESTAMP " +
                    "WHERE corporate_email = :email";

    public void saveConfirmationToken(String email, String token, LocalDateTime expiry) {
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("token", token);
        params.put("expiry", expiry);

        jdbcTemplate.update(SAVE_TOKEN, params);
    }

    private static final String VALIDATE_TOKEN =
            "UPDATE chatbot.brl_form_submission " +
                    "SET email_confirmed = true, " +
                    "confirmed_at = CURRENT_TIMESTAMP, " +
                    "updated_at = CURRENT_TIMESTAMP " +
                    "WHERE confirmation_token = :token " +
                    "AND token_expiry > CURRENT_TIMESTAMP " +
                    "AND email_confirmed = false " +
                    "AND confirmation_token IS NOT NULL " +
                    "RETURNING corporate_email";

    public String validateToken(String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);

        try {
            return jdbcTemplate.queryForObject(VALIDATE_TOKEN, params, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateFormSubmissionIds(Long companyId, Long personId, Long roleId, String email) {
        String sql = """
            UPDATE chatbot.brl_form_submission 
            SET company_id = :companyId,
                person_id = :personId,
                role_id = :roleId,
                updated_at = CURRENT_TIMESTAMP
            WHERE corporate_email = :email
              AND company_id IS NULL
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("personId", personId)
                .addValue("roleId", roleId)
                .addValue("email", email);

        int updatedRows = jdbcTemplate.update(sql, params);
        if (updatedRows > 0) {
            log.info("Successfully updated form submission. companyId: {}, personId: {}, roleId: {}, email: {}",
                    companyId, personId, roleId, email);
        } else {
            log.warn("No rows updated for email: {}. This could mean the record was already updated or doesn't exist.",
                    email);
        }
    }
}

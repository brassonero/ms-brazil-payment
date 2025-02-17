package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.entity.FormSubmission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.ebitware.chatbotpayments.model.FormSubmissionRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public Optional<FormSubmission> findByPersonId(Integer personId) {
        String sql = """
                SELECT id, business_name, display_name, website, corporate_email, 
                       description, facebook_manager_no, phone, address, vertical, 
                       logo_url, created_at, updated_at, company_id, person_id, role_id
                FROM chatbot.brl_form_submission 
                WHERE person_id = :personId
                """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("personId", personId);

            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params,
                    (rs, rowNum) -> new FormSubmission(
                            rs.getLong("id"),
                            rs.getString("business_name"),
                            rs.getString("display_name"),
                            rs.getString("website"),
                            rs.getString("corporate_email"),
                            rs.getString("description"),
                            rs.getString("facebook_manager_no"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("vertical"),
                            rs.getString("logo_url"),
                            rs.getObject("created_at", OffsetDateTime.class),
                            rs.getObject("updated_at", OffsetDateTime.class),
                            rs.getInt("company_id"),
                            rs.getInt("person_id"),
                            rs.getInt("role_id")
                    )));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int updateLogoUrl(Integer personId, String logoUrl) {
        String sql = """
                    UPDATE chatbot.brl_form_submission 
                    SET logo_url = :logoUrl,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE person_id = :personId
                    RETURNING id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("logoUrl", logoUrl)
                .addValue("personId", personId);

        try {
            return jdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            log.debug("No form submission found for person ID: {}", personId);
            return 0;
        }
    }
}

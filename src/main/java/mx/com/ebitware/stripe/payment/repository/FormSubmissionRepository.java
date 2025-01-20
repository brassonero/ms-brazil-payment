package mx.com.ebitware.stripe.payment.repository;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.FormSubmissionRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static mx.com.ebitware.stripe.payment.constants.SqlConstants.CHECK_EMAIL_EXISTS;
import static mx.com.ebitware.stripe.payment.constants.SqlConstants.INSERT_FORM_SUBMISSION;

@Repository
@RequiredArgsConstructor
public class FormSubmissionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

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

        Integer count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);
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
}

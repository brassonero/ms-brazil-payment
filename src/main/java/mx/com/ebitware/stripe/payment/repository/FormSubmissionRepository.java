package mx.com.ebitware.stripe.payment.repository;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.FormSubmissionRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.Map;

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
}

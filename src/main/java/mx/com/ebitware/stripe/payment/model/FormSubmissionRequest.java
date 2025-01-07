package mx.com.ebitware.stripe.payment.model;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class FormSubmissionRequest {
    @NotNull(message = "Business name is required")
    @NotBlank(message = "Business name cannot be empty")
    private String businessName;
    private String displayName;
    @NotNull(message = "Corporate email is required")
    @Email(message = "Please provide a valid email address")
    private String corporateEmail;
    @URL(message = "Please provide a valid website URL")
    private String website;
    private String description;
    private String facebookManagerNo;
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phone;
    private String address;
    private String vertical;
}

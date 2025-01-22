package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeInvoiceRequest {
    @NotBlank(message = "Person type is required")
    private String personType;
    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name must be less than 150 characters")
    private String businessName;
    @NotBlank(message = "ID type is required")
    private String idType;
    @NotBlank(message = "ID number is required")
    @Pattern(regexp = "\\d+", message = "ID number must contain only digits")
    private String idNumber;
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+?\\d{7,15}", message = "Phone number must be valid")
    private String phone;
    @NotBlank(message = "Fiscal address is required")
    @Size(max = 200, message = "Fiscal address must be less than 200 characters")
    private String fiscalAddress;
    @NotBlank(message = "Department is required")
    private String department;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "Country is required")
    private String country;
    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "\\d{4,10}", message = "Postal code must be valid")
    private String postalCode;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must be less than 100 characters")
    private String customerName;
    @NotNull(message = "Amount in cents is required")
    private Long amountInCents;
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;
}

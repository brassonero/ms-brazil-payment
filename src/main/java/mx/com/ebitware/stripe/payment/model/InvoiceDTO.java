package mx.com.ebitware.stripe.payment.model;


import lombok.Data;

@Data
public class InvoiceDTO {
    private Long id;
    private String fiscalRegime;
    private String businessName;
    private String idType;
    private String idNumber;
    private String billingEmail;
    private String phone;
    private String street;
    private String state;
    private String city;
    private String country;
    private String postalCode;
    private String taxId;
    private String cfdiUsage;
    private String neighborhood;
}

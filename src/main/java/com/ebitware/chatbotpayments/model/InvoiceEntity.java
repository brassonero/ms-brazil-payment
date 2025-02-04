package com.ebitware.chatbotpayments.model;


import lombok.Data;

@Data
public class InvoiceEntity {
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
    private Integer personId;
}

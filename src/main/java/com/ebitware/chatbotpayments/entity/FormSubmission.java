package com.ebitware.chatbotpayments.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmission {
    private Long id;
    private String businessName;
    private String displayName;
    private String website;
    private String corporateEmail;
    private String description;
    private String facebookManagerNo;
    private String phone;
    private String address;
    private String vertical;
    private String logoUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer companyId;
    private Integer personId;
    private Integer roleId;

    public FormSubmission(Long id, String businessName, String corporateEmail,
                          Integer personId, Integer companyId, Integer roleId) {
        this.id = id;
        this.businessName = businessName;
        this.corporateEmail = corporateEmail;
        this.personId = personId;
        this.companyId = companyId;
        this.roleId = roleId;
    }
}

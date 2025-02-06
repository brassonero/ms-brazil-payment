package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyCreationResult {
    private Long companyId;
    private Long personId;
    private Long roleId;
}

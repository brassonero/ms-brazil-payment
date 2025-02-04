package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class CompanyCreationResult {
    private Long companyId;
    private Long personId;
    private Long roleId;
}

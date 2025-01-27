package com.ebitware.chatbotpayments.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class CompanyCreationResult {
    private Long companyId;
    private Long userId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime userCreatedAt;
    private ZonedDateTime userUpdatedAt;
}

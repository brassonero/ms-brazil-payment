package com.ebitware.chatbotpayments.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class CompanyResponse {
    private Long id;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private UserResponseDTO user;
}

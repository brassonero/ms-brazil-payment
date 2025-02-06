package com.ebitware.chatbotpayments.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}

package com.ebitware.chatbotpayments.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CompanyResponse {
    private int httpStatus;
    private String message;
    private CompanyData data;

    @Data
    public static class CompanyData {
        private Long id;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private UserData user;
    }

    @Data
    public static class UserData {
        private Long id;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}


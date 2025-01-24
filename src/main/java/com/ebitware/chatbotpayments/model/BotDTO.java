package com.ebitware.chatbotpayments.model;

import lombok.Data;

@Data
public class BotDTO {
    private String name;
    private String port;
    private String customMessage;
    private String message;
    private String status;
    private String botChannel = "default_channel";
}

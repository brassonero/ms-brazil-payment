package com.ebitware.chatbotpayments.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BotDTO {
    @NotNull
    @Size(max = 50)
    private String name;

    @NotNull
    @Size(max = 10)
    private String port;

    @Size(max = 255)
    private String customMessage;

    @Size(max = 255)
    private String message;

    @NotNull
    @Size(max = 20)
    private String status;

    @NotNull
    @Size(max = 50)
    private String botChannel = "default_channel";
}

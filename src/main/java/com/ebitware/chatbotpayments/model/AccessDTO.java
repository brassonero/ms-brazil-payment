package com.ebitware.chatbotpayments.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;


@Data
public class AccessDTO {
    private Long id;

    @NotNull
    @Size(max = 100)
    private String name;

    @NotNull
    @Size(max = 50)
    private String keyName;

    private ZonedDateTime createdAt;
}

package com.ebitware.chatbotpayments.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceRequest {

    private Long productId;

    @NotNull
    @Min(1)
    private Long unitAmount;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "Currency must be a 3-letter code")
    private String currency;

    @NotNull
    @Pattern(regexp = "^(day|week|month|year)$", message = "Interval must be one of: day, week, month, year")
    private String interval;

    private JsonNode metadata;
}

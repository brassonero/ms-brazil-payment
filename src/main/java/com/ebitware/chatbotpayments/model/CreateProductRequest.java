package com.ebitware.chatbotpayments.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    private String description;

    private JsonNode metadata;
}

package com.ebitware.chatbotpayments.entity;

import lombok.Getter;

@Getter
public enum DocumentType {
    CPF("CPF"),
    CNPJ("CNPJ");

    private final String type;

    DocumentType(String type) {
        this.type = type;
    }

    public static DocumentType fromString(String text) {
        for (DocumentType type : DocumentType.values()) {
            if (type.type.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}

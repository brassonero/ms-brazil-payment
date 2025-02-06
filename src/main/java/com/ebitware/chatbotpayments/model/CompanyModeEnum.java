package com.ebitware.chatbotpayments.model;

import lombok.Getter;

@Getter
public enum CompanyModeEnum {
    PLATFORM("1"),
    API("2"),
    SAMPLER("3"),
    CUSTOMER("4");

    private final String code;

    CompanyModeEnum(String code) {
        this.code = code;
    }

    public static CompanyModeEnum fromCode(String code) {
        for (CompanyModeEnum mode : CompanyModeEnum.values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}

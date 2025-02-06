package com.ebitware.chatbotpayments.model;

import lombok.Getter;

@Getter
public enum WorkgroupEnum {
    IVR("ivr"),
    PERSONALIZED("personalized"),
    PREDETERMINED("predetermined");

    private final String value;

    WorkgroupEnum(String value) {
        this.value = value;
    }

    public static WorkgroupEnum fromValue(String value) {
        for (WorkgroupEnum workgroup : WorkgroupEnum.values()) {
            if (workgroup.value.equalsIgnoreCase(value)) {
                return workgroup;
            }
        }
        throw new IllegalArgumentException("Invalid WorkgroupEnum value: " + value);
    }
}

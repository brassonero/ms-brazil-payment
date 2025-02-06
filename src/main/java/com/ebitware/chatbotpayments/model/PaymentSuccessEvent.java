package com.ebitware.chatbotpayments.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentSuccessEvent {
    String companyName;
    String bmId;
    String commercialName;
    String phone;
    String email;
    String website;
    String address;
    String commercialInfo;
    String vertical;
    String businessDescription;
    String planName;
    String planValue;
    String currency;
    String contractPeriod;
    String startDate;
    String endDate;
    String agents;
    String addons;
    String monthlyVolume;
    String channels;
    String paymentDate;
    String setupFee;
    String customerEmail;
}

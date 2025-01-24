package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.client.SecurityClient;
import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyResponse;
import com.ebitware.chatbotpayments.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final SecurityClient securityClient;

    @Override
    public CompanyResponse createCompany(CompanyRequest request, String token) {
        return securityClient.createCompany(token, request);
    }
}

package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.client.SecurityClient;
import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyClientResponse;
import com.ebitware.chatbotpayments.service.CompanyClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyClientClientServiceImpl implements CompanyClientService {

    private final SecurityClient securityClient;

    @Override
    public CompanyClientResponse createCompany(CompanyRequest request, String token) {
        return securityClient.createCompany(token, request);
    }
}

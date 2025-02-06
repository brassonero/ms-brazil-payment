package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyClientResponse;

public interface CompanyClientService {
    CompanyClientResponse createCompany(CompanyRequest request, String token);
}

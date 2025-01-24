package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyResponse;

public interface CompanyService {
    CompanyResponse createCompany(CompanyRequest request, String token);
}

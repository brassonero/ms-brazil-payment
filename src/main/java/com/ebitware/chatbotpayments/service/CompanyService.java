package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.WorkspaceDTO;

import java.util.Map;

public interface CompanyService {
    Long createCompany(WorkspaceDTO request);
    Map<String, Object> getCompanyDetails(Long companyId);
}

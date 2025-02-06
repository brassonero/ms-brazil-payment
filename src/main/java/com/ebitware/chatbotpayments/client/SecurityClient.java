package com.ebitware.chatbotpayments.client;

import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "security-client", url = "${api.security.base-url}")
public interface SecurityClient {
    @PostMapping("/api/security/companies")
    CompanyClientResponse createCompany(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CompanyRequest request
    );
}

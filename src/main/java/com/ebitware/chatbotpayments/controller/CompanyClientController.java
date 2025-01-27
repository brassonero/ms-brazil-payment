package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyClientResponse;
import com.ebitware.chatbotpayments.service.CompanyClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/companies")
@RequiredArgsConstructor
public class CompanyClientController {

    private final CompanyClientService companyClientService;

    @PostMapping
    public ResponseEntity<CompanyClientResponse> createCompany(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CompanyRequest request) {
        CompanyClientResponse response = companyClientService.createCompany(request, authorization);
        return ResponseEntity.ok(response);
    }
}

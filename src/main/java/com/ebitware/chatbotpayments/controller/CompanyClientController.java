package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.CompanyRequest;
import com.ebitware.chatbotpayments.model.CompanyResponse;
import com.ebitware.chatbotpayments.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/companies")
@RequiredArgsConstructor
public class CompanyClientController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request, authorization);
        return ResponseEntity.ok(response);
    }
}

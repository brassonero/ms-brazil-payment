package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.WorkspaceDTO;
import com.ebitware.chatbotpayments.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCompany(
            @Valid @RequestBody WorkspaceDTO request) {
        Long companyId = companyService.createCompany(request);

        Map<String, Object> companyDetails = companyService.getCompanyDetails(companyId);

        Map<String, Object> response = new HashMap<>();
        response.put("httpStatus", HttpStatus.CREATED.value());
        response.put("message", "Company created successfully");
        response.put("data", companyDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

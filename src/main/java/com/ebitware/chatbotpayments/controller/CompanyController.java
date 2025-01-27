package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.WorkspaceDTO;
import com.ebitware.chatbotpayments.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<Long> createCompany(
            @Valid
            @RequestBody WorkspaceDTO request) {
        Long companyId = companyService.createCompany(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(companyId);
    }
}

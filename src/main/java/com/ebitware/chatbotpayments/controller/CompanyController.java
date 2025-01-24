package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.WorkspaceDTO;
import com.ebitware.chatbotpayments.service.impl.PersonServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final PersonServiceImpl personService;

    @PostMapping
    public ResponseEntity<Long> createCompany(@RequestBody WorkspaceDTO request) {
        Long companyId = personService.createCompany(request);
        return ResponseEntity.ok(companyId);
    }
}

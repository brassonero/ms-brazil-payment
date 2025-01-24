package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.entity.Person;
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
    public ResponseEntity<Void> createCompany(@RequestBody WorkspaceDTO request) {
        personService.validateEmail(request);
        return ResponseEntity.ok().build();
    }
}

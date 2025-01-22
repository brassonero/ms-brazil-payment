package com.ebitware.chatbotpayments.controller;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.FormSubmissionRequest;
import com.ebitware.chatbotpayments.service.FormSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forms")
public class FormSubmissionController {

    private final FormSubmissionService formSubmissionService;

    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> submitForm(@Valid @RequestBody FormSubmissionRequest form) {
        if (!formSubmissionService.isEmailAvailable(form.getCorporateEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email is already registered");
        }
        formSubmissionService.saveSubmission(form, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Successful submission");
    }
}

package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.model.LoginCredentials;
import com.ebitware.chatbotpayments.model.LoginRequest;
import com.ebitware.chatbotpayments.model.LoginResponse;
import com.ebitware.chatbotpayments.service.impl.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        LoginCredentials credentials = authService.decryptCredentials(request.getEncryptedCredentials());

        log.info("{}",credentials);

        return ResponseEntity.ok("OK");
    }
}

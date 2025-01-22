package com.ebitware.chatbotpayments.controller;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.PlansCatalogResponse;
import com.ebitware.chatbotpayments.service.PlansCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans")
public class PlansCatalogController {

    private final PlansCatalogService plansCatalogService;

    @GetMapping("/catalog")
    public CompletableFuture<ResponseEntity<PlansCatalogResponse>> getCatalog() {
        return plansCatalogService.getCatalog()
                .thenApply(ResponseEntity::ok);
    }
}

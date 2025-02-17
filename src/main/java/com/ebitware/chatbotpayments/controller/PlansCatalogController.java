package com.ebitware.chatbotpayments.controller;

import com.stripe.Stripe;
import com.ebitware.chatbotpayments.model.PlansCatalogResponse;
import com.ebitware.chatbotpayments.service.PlansCatalogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/plans")
public class PlansCatalogController {

    private final PlansCatalogService plansCatalogService;


    public PlansCatalogController(@Value("${stripe.secret-key}") String stripeApiKey, PlansCatalogService plansCatalogService) {
        this.plansCatalogService = plansCatalogService;
        Stripe.apiKey = stripeApiKey;
    }

    @GetMapping("/catalog")
    public CompletableFuture<ResponseEntity<PlansCatalogResponse>> getCatalog() {
        return plansCatalogService.getCatalog()
                .thenApply(ResponseEntity::ok);
    }
}

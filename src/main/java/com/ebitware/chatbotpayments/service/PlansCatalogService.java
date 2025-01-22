package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.PlansCatalogResponse;

import java.util.concurrent.CompletableFuture;

public interface PlansCatalogService {
    CompletableFuture<PlansCatalogResponse> getCatalog();
}

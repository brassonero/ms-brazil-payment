package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.PlansCatalogResponse;
import com.ebitware.chatbotpayments.model.PlansDTO;
import com.ebitware.chatbotpayments.model.PricesDTO;
import com.ebitware.chatbotpayments.repository.billing.PlansCatalogRepository;
import com.ebitware.chatbotpayments.repository.billing.PricesCatalogRepository;
import com.ebitware.chatbotpayments.service.PlansCatalogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PlansCatalogServiceImpl implements PlansCatalogService {

    private final PlansCatalogRepository plansCatalogRepository;
    private final PricesCatalogRepository pricesCatalogRepository;

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<PlansDTO>> getPlansAsync() {
        return CompletableFuture.completedFuture(plansCatalogRepository.findAllPlans());
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<PricesDTO>> getPricesAsync() {
        return CompletableFuture.completedFuture(pricesCatalogRepository.findAllPricing());
    }

    @Override
    public CompletableFuture<PlansCatalogResponse> getCatalog() {
        CompletableFuture<List<PlansDTO>> plansFuture = getPlansAsync();
        CompletableFuture<List<PricesDTO>> pricesFuture = getPricesAsync();

        return CompletableFuture.allOf(plansFuture, pricesFuture)
                .thenApply(v -> PlansCatalogResponse.builder()
                        .plans(plansFuture.join())
                        .waPrices(pricesFuture.join())
                        .build());
    }
}

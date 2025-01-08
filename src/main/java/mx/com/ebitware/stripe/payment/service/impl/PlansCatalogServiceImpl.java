package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PlansCatalogResponse;
import mx.com.ebitware.stripe.payment.model.PlansDTO;
import mx.com.ebitware.stripe.payment.model.PricesDTO;
import mx.com.ebitware.stripe.payment.repository.PlansCatalogRepository;
import mx.com.ebitware.stripe.payment.repository.PricesCatalogRepository;
import mx.com.ebitware.stripe.payment.service.PlansCatalogService;
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

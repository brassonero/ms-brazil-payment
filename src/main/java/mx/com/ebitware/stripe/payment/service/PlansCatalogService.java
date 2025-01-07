package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.PlansCatalogResponse;

import java.util.concurrent.CompletableFuture;

public interface PlansCatalogService {
    CompletableFuture<PlansCatalogResponse> getCatalog();
}

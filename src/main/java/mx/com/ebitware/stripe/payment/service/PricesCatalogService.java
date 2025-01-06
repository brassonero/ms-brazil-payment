package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.PricesCatalogResponse;

import java.util.List;

public interface PricesCatalogService {
    List<PricesCatalogResponse> getAllPricing();
}

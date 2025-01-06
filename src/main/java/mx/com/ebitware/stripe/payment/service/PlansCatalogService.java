package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.PlansCatalogResponse;

import java.util.List;

public interface PlansCatalogService {
    List<PlansCatalogResponse> getAllPlans();
}

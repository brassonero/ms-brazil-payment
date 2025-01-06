package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PlansCatalogResponse;
import mx.com.ebitware.stripe.payment.repository.PlansCatalogRepository;
import mx.com.ebitware.stripe.payment.service.PlansCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlansCatalogServiceImpl implements PlansCatalogService {

    private final PlansCatalogRepository plansCatalogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlansCatalogResponse> getAllPlans() {
        return plansCatalogRepository.findAllPlans();
    }
}

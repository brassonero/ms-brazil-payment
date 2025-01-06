package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PricesCatalogResponse;
import mx.com.ebitware.stripe.payment.repository.PricesCatalogRepository;
import mx.com.ebitware.stripe.payment.service.PricesCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PricesCatalogServiceImpl implements PricesCatalogService {

    private final PricesCatalogRepository pricesCatalogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PricesCatalogResponse> getAllPricing() {
        return pricesCatalogRepository.findAllPricing();
    }
}

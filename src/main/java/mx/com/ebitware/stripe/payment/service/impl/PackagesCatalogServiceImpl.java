package mx.com.ebitware.stripe.payment.service.impl;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PackagesCatalogResponse;
import mx.com.ebitware.stripe.payment.repository.PackagesCatalogRepository;
import mx.com.ebitware.stripe.payment.service.PackagesCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackagesCatalogServiceImpl implements PackagesCatalogService {

    private final PackagesCatalogRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PackagesCatalogResponse> getAllPackages() {
        return repository.findAllPackages();
    }
}

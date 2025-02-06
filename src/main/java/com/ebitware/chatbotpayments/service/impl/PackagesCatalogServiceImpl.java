package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.PackagesCatalogResponse;
import com.ebitware.chatbotpayments.repository.billing.PackagesCatalogRepository;
import com.ebitware.chatbotpayments.service.PackagesCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackagesCatalogServiceImpl implements PackagesCatalogService {

    private final PackagesCatalogRepository packagesCatalogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PackagesCatalogResponse> getAllPackages() {
        return packagesCatalogRepository.findAllPackages();
    }
}

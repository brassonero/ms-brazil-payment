package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.PackagesCatalogResponse;

import java.util.List;

public interface PackagesCatalogService {
    List<PackagesCatalogResponse> getAllPackages();
}

package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.PackagesCatalogResponse;

import java.util.List;

public interface PackagesCatalogService {
    List<PackagesCatalogResponse> getAllPackages();
}

package mx.com.ebitware.stripe.payment.controller;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PackagesCatalogResponse;
import mx.com.ebitware.stripe.payment.repository.PackagesCatalogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/packages")
public class PackagesCatalogController {

    private final PackagesCatalogRepository repository;

    @GetMapping("/catalog")
    public ResponseEntity<List<PackagesCatalogResponse>> getCatalog() {
        return ResponseEntity.ok(repository.findAllPackages());
    }
}

package mx.com.ebitware.stripe.payment.controller;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PackagesCatalogResponse;
import mx.com.ebitware.stripe.payment.service.PackagesCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/packages")
public class PackagesCatalogController {

    private final PackagesCatalogService packagesCatalogService;

    @GetMapping("/catalog")
    public ResponseEntity<List<PackagesCatalogResponse>> getPackages() {
        return ResponseEntity.ok(packagesCatalogService.getAllPackages());
    }
}

package mx.com.ebitware.stripe.payment.controller;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PackagesCatalogResponse;
import mx.com.ebitware.stripe.payment.service.PackagesCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/packages")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://127.0.0.1:5173"},
        allowedHeaders = {"Content-Type", "Accept", "Authorization", "Origin"},
        methods = {RequestMethod.GET, RequestMethod.OPTIONS}
)
public class PackagesCatalogController {

    private final PackagesCatalogService packagesCatalogService;

    @GetMapping("/catalog")
    public ResponseEntity<List<PackagesCatalogResponse>> getPackages() {
        return ResponseEntity.ok(packagesCatalogService.getAllPackages());
    }
}

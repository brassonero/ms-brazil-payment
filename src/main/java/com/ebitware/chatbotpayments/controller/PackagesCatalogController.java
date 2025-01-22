package com.ebitware.chatbotpayments.controller;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.PackagesCatalogResponse;
import com.ebitware.chatbotpayments.service.PackagesCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/packages")
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

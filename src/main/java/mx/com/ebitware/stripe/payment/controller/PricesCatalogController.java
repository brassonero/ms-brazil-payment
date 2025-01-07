package mx.com.ebitware.stripe.payment.controller;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PricesCatalogResponse;
import mx.com.ebitware.stripe.payment.service.PricesCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/wa-prices")
public class PricesCatalogController {

    private final PricesCatalogService pricesCatalogService;

    @GetMapping("/catalog")
    public ResponseEntity<List<PricesCatalogResponse>> getPrices() {
        return ResponseEntity.ok(pricesCatalogService.getAllPricing());
    }
}

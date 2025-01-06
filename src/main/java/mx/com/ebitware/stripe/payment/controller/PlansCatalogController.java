package mx.com.ebitware.stripe.payment.controller;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PlansCatalogResponse;
import mx.com.ebitware.stripe.payment.service.PlansCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/plans")
public class PlansCatalogController {

    private final PlansCatalogService plansCatalogService;

    @GetMapping("/catalog")
    public ResponseEntity<List<PlansCatalogResponse>> getPlansCatalog() {
        return ResponseEntity.ok(plansCatalogService.getAllPlans());
    }
}

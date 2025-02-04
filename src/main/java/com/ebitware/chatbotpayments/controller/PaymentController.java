package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.*;
import com.ebitware.chatbotpayments.model.CreatePriceRequest;
import com.ebitware.chatbotpayments.model.CreateProductRequest;
import com.ebitware.chatbotpayments.service.PaymentService;
import com.ebitware.chatbotpayments.service.impl.BrlPriceService;
import com.ebitware.chatbotpayments.service.impl.BrlProductService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"*"},
        allowedHeaders = {"Content-Type", "Accept", "Authorization", "Origin"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class PaymentController {

    private final PaymentService paymentService;
    private final BrlProductService brlProductService;
    private final BrlPriceService brlPriceService;

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        log.info("Received payment request");

        try {
            if (!payload.containsKey("person_id")) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "person_id is required");
            }

            int personId;
            try {
                personId = Integer.parseInt(payload.get("person_id").toString());
            } catch (NumberFormatException e) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid person_id format");
            }

            Map<String, Object> result = paymentService.processPayment(payload, personId);
            return ResponseEntity.ok(result);
        } catch (PaymentValidationException e) {
            log.error("Payment validation error: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error occurred: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message) {
        return createErrorResponse(status, message, null);
    }

    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, String code) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        if (code != null) {
            response.put("code", code);
        }
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/products")
    public ResponseEntity<BrlProduct> createProduct(@RequestBody CreateProductRequest request) {
        try {
            BrlProduct product = brlProductService.createProduct(request);
            return ResponseEntity.ok(product);
        } catch (ProductException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("products/{productId}/prices")
    public ResponseEntity<BrlPrice> createPrice(
            @PathVariable Long productId,
            @Valid @RequestBody CreatePriceRequest request) {
        try {
            request.setProductId(productId);
            BrlPrice price = brlPriceService.createPrice(request);
            return ResponseEntity.ok(price);
        } catch (ProductNotFoundException e) {
            log.warn("Product not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PriceException e) {
            log.error("Error creating price: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating price", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ExceptionHandler(StripeApiException.class)
    public ResponseEntity<ErrorResponse> handleStripeApiException(StripeApiException e) {
        log.error("Stripe API error: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), e.getStatus().value());
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getPaymentHistory() {
        List<Map<String, Object>> paymentHistory = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Map<String, Object> payment = new HashMap<>();
            payment.put("date", "17 Mar 2024");
            payment.put("receiptNumber", "14345980");
            payment.put("totalAmount", "$14345");
            paymentHistory.add(payment);
        }

        return ResponseEntity.ok(paymentHistory);
    }

    @GetMapping("/{userId}/methods")
    public ResponseEntity<List<Map<String, Object>>> getPaymentMethods(@PathVariable String userId) {

        List<Map<String, Object>> paymentMethods = new ArrayList<>();

        Map<String, Object> method1 = new HashMap<>();
        method1.put("cardType", "MasterCard");
        method1.put("cardNumber", "**** **** **** 1234");
        method1.put("cardholderName", "Ixchel Gómez García");
        method1.put("bank", "Santander");
        method1.put("isSelected", true);

        Map<String, Object> method2 = new HashMap<>();
        method2.put("cardType", "Visa");
        method2.put("cardNumber", "**** **** **** 3478");
        method2.put("cardholderName", "Ixchel Gómez García");
        method2.put("bank", "BBVA");
        method2.put("isSelected", false);

        Map<String, Object> method3 = new HashMap<>();
        method3.put("cardType", "Visa");
        method3.put("cardNumber", "**** **** **** 2389");
        method3.put("cardholderName", "Ixchel Gómez García");
        method3.put("bank", "HSBC");
        method3.put("isSelected", false);

        paymentMethods.add(method1);
        paymentMethods.add(method2);
        paymentMethods.add(method3);

        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/{userId}/payment-methods")
    public ResponseEntity<?> listUserPaymentMethods(@PathVariable String userId) {
        try {
            List<Map<String, Object>> paymentMethods = paymentService.listPaymentMethodsFormatted(userId);
            return ResponseEntity.ok(paymentMethods);
        } catch (PaymentValidationException e) {
            log.error("Validation error retrieving payment methods: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error retrieving payment methods: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error retrieving payment methods: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @GetMapping("/receipts")
    public ResponseEntity<?> getPaymentReceipts(
            @RequestParam(required = false) String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, String>> receipts = paymentService.getPaymentReceipts(customerId, page, size);
            return ResponseEntity.ok(receipts);
        } catch (PaymentValidationException e) {
            log.error("Validation error retrieving receipts: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving receipts: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @GetMapping("/receipts/{paymentIntentId}/download")
    public ResponseEntity<?> downloadReceipt(@PathVariable String paymentIntentId) {
        try {
            String receiptUrl = paymentService.getPaymentReceipt(paymentIntentId);
            if (receiptUrl != null) {
                // Redirect to Stripe's hosted receipt URL
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(receiptUrl))
                        .build();
            }
            return ResponseEntity.notFound().build();
        } catch (PaymentValidationException e) {
            log.error("Validation error retrieving receipt: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error retrieving receipt: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving receipt: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }
}

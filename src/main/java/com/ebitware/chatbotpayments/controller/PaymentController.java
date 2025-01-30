package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.entity.BrlSubscription;
import com.ebitware.chatbotpayments.exception.*;
import com.ebitware.chatbotpayments.model.CreateCustomerRequest;
import com.ebitware.chatbotpayments.model.CreatePriceRequest;
import com.ebitware.chatbotpayments.model.CreateProductRequest;
import com.ebitware.chatbotpayments.model.CreateSubscriptionRequest;
import com.ebitware.chatbotpayments.service.PaymentService;
import com.ebitware.chatbotpayments.service.impl.BrlCustomerService;
import com.ebitware.chatbotpayments.service.impl.BrlPriceService;
import com.ebitware.chatbotpayments.service.impl.BrlProductService;
import com.ebitware.chatbotpayments.service.impl.BrlSubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5173", "http://127.0.0.1:5173"},
        allowedHeaders = {"Content-Type", "Accept", "Authorization", "Origin"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class PaymentController {

    private final PaymentService paymentService;
    private final BrlProductService brlProductService;
    private final BrlPriceService brlPriceService;
    private final BrlCustomerService brlCustomerService;
    private final BrlSubscriptionService brlSubscriptionService;

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody Map<String, Object> payload) {
        log.info("Creating customer with payload: {}", payload);

        try {
            String email = validateAndGetString(payload, "email");
            String name = validateAndGetString(payload, "name");
            String stripeToken = validateAndGetString(payload, "stripeToken");

            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setEmail(email);
            request.setName(name);
            request.setSource(stripeToken);

            if (payload.containsKey("metadata")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode metadata = mapper.valueToTree(payload.get("metadata"));
                request.setMetadata(metadata);
            }

            BrlCustomer customer = brlCustomerService.createCustomer(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "customer", customer
            ));

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (CustomerException e) {
            log.error("Customer creation error: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating customer", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private String validateAndGetString(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        if (value == null || !(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return ((String) value).trim();
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        log.info("Received payment request");

        try {
            Map<String, Object> result = paymentService.processPayment(payload);
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

    @PostMapping("/subscriptions")
    public ResponseEntity<?> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Creating subscription for customer: {}, price: {}", request.getCustomerId(), request.getPriceId());

        try {
            BrlSubscription subscription = brlSubscriptionService.createSubscription(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "subscription", subscription
            ));
        } catch (CustomerNotFoundException | PriceNotFoundException e) {
            log.warn(e.getMessage());
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (SubscriptionException e) {
            log.error("Subscription creation error: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating subscription", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }
}

package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.*;
import com.ebitware.chatbotpayments.model.ChangeSubscriptionRequest;
import com.ebitware.chatbotpayments.model.CreatePriceRequest;
import com.ebitware.chatbotpayments.model.CreateProductRequest;
import com.ebitware.chatbotpayments.model.TransactionDTO;
import com.ebitware.chatbotpayments.service.PaymentService;
import com.ebitware.chatbotpayments.service.impl.BrlPriceService;
import com.ebitware.chatbotpayments.service.impl.BrlProductService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BrlProductService brlProductService;
    private final BrlPriceService brlPriceService;

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        log.info("Received payment request");

        try {
            if (!payload.containsKey("personId")) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "personId is required");
            }

            int personId;
            try {
                personId = Integer.parseInt(payload.get("personId").toString());
            } catch (NumberFormatException e) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid personId format");
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
    public ResponseEntity<List<TransactionDTO>> getCustomerTransactions(
            @RequestParam String customerId) {
        try {
            List<TransactionDTO> transactions = paymentService.getCustomerTransactions(customerId);
            return ResponseEntity.ok(transactions);
        } catch (StripeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/receipts/{paymentIntentId}/download")
    public ResponseEntity<?> downloadReceipt(@PathVariable String paymentIntentId) {
        try {
            String receiptUrl = paymentService.getPaymentReceipt(paymentIntentId);
            if (receiptUrl != null) {
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

    @PostMapping("/{userId}/setup-intent")
    public ResponseEntity<?> createSetupIntent(@PathVariable String userId) {
        try {
            Map<String, String> setupIntent = paymentService.createSetupIntent(userId);
            return ResponseEntity.ok(setupIntent);
        } catch (PaymentValidationException e) {
            log.error("Validation error creating setup intent: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error creating setup intent: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        }
    }

    @GetMapping("/subscriptions/{subscriptionId}/receipts")
    public ResponseEntity<?> downloadSubscriptionReceipt(@PathVariable String subscriptionId) {
        try {
            String receiptUrl = paymentService.getSubscriptionReceipt(subscriptionId);
            if (receiptUrl != null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(receiptUrl))
                        .build();
            }
            return ResponseEntity.notFound().build();
        } catch (PaymentValidationException e) {
            log.error("Validation error retrieving subscription receipt: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error retrieving subscription receipt: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving subscription receipt: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @PutMapping("/subscriptions/{subscriptionId}/change")
    public ResponseEntity<?> changeSubscription(
            @PathVariable String subscriptionId,
            @Valid @RequestBody ChangeSubscriptionRequest payload
    ) {
        log.info("Received subscription change request for subscription: {}", subscriptionId);

        try {
            Map<String, Object> result = paymentService.changeSubscription(subscriptionId, payload);
            return ResponseEntity.ok(result);
        } catch (PaymentValidationException e) {
            log.error("Subscription change validation error: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error during subscription change: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error during subscription change: {}", e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @GetMapping("/subscriptions/{customerId}/plan-details")
    public ResponseEntity<?> getPlanDetails(@PathVariable String customerId) {
        log.info("Retrieving plan details for customer: {}", customerId);

        try {
            Map<String, String> details = paymentService.getPlanDetails(customerId);
            return ResponseEntity.ok(details);
        } catch (PaymentValidationException e) {
            log.error("Validation error retrieving plan details: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving plan details: {}", e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @DeleteMapping("/{userId}/payment-methods/{paymentMethodId}")
    public ResponseEntity<?> deletePaymentMethod(
            @PathVariable String userId,
            @PathVariable String paymentMethodId) {
        try {
            paymentService.deletePaymentMethod(userId, paymentMethodId);
            return ResponseEntity.noContent().build();
        } catch (PaymentValidationException e) {
            log.error("Validation error deleting payment method: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error deleting payment method: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error deleting payment method: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @GetMapping("/{userId}/subscription-status")
    public ResponseEntity<?> getSubscriptionStatus(@PathVariable String userId) {
        try {
            Map<String, Object> status = paymentService.getSubscriptionStatus(userId);
            return ResponseEntity.ok(status);
        } catch (PaymentValidationException e) {
            log.error("Validation error getting subscription status: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error getting subscription status: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error getting subscription status: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @DeleteMapping("/{userId}/subscriptions/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable String userId,
            @PathVariable String subscriptionId,
            @RequestParam(defaultValue = "false") boolean cancelAtPeriodEnd) {
        try {
            Map<String, Object> result = paymentService.cancelSubscription(userId, subscriptionId, cancelAtPeriodEnd);
            return ResponseEntity.ok(result);
        } catch (PaymentValidationException e) {
            log.error("Validation error canceling subscription: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (StripeException e) {
            log.error("Stripe error canceling subscription: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error canceling subscription: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @PutMapping("/{userId}/payment-methods/{paymentMethodId}/make-default")
    public ResponseEntity<?> updateDefaultPaymentMethod(
            @PathVariable String userId,
            @PathVariable String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            if (paymentMethod.getCustomer() == null) {
                PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                        .setCustomer(userId)
                        .build();
                paymentMethod = paymentMethod.attach(attachParams);
            }
            // If it's attached to a different customer, detach and reattach
            else if (!userId.equals(paymentMethod.getCustomer())) {
                paymentMethod = paymentMethod.detach();
                PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                        .setCustomer(userId)
                        .build();
                paymentMethod = paymentMethod.attach(attachParams);
            }

            CustomerUpdateParams customerParams = CustomerUpdateParams.builder()
                    .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(paymentMethodId)
                                    .build()
                    )
                    .build();

            Customer.retrieve(userId).update(customerParams);

            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(userId)
                    .setStatus(SubscriptionListParams.Status.ACTIVE)
                    .build();

            SubscriptionCollection subscriptions = Subscription.list(params);
            for (Subscription subscription : subscriptions.getData()) {
                SubscriptionUpdateParams updateParams = SubscriptionUpdateParams.builder()
                        .setDefaultPaymentMethod(paymentMethodId)
                        .build();
                subscription.update(updateParams);
            }

            log.info("Successfully updated default payment method to {} for user {}",
                    paymentMethodId, userId);

            return ResponseEntity.ok().build();
        } catch (StripeException e) {
            log.error("Stripe error updating default payment method: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error updating default payment method: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }
}

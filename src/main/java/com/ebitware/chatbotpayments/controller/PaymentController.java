package com.ebitware.chatbotpayments.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://127.0.0.1:5173"},
        allowedHeaders = {"Content-Type", "Accept", "Authorization", "Origin"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class PaymentController {

    private static final String STRIPE_SECRET_KEY = "sk_test_51QeiWQQL0OOvl0KQX3CXSLCYeuY9o1lMgOOINRrTGdgRhfC0f7R2GmNhCPVIPL5f7O0WtndYS8SFrk0eHrM9DCOk00tZKkEHNB";
    private static final long MIN_AMOUNT = 50L; // $0.50
    private static final long MAX_AMOUNT = 20000000L; // $10000

    public PaymentController() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        log.info("Received payment request");

        // Validate stripe token
        String stripeToken = (String) payload.get("stripeToken");
        if (stripeToken == null || stripeToken.isEmpty()) {
            log.error("Stripe token is missing");
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Stripe token is required");
        }

        // Validate and extract amount
        Long amount = validateAndExtractAmount(payload);
        if (amount == null) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid amount provided");
        }

        // Validate amount range
        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            log.error("Amount {} is outside allowed range", amount);
            return createErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    String.format("Amount must be between $%.2f and $%.2f", MIN_AMOUNT / 100.0, MAX_AMOUNT / 100.0)
            );
        }

        try {
            // Create charge parameters
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", amount);
            chargeParams.put("currency", "mxn");
            chargeParams.put("source", stripeToken);
            chargeParams.put("description", "Dynamic Amount Payment");

            log.info("Creating charge for token: {} with amount: {}", stripeToken, amount);
            Charge charge = Charge.create(chargeParams);
            log.info("Charge created successfully: {}", charge.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "transactionId", charge.getId(),
                    "amount", charge.getAmount(),
                    "currency", charge.getCurrency(),
                    "description", charge.getDescription()
            ));

        } catch (StripeException e) {
            log.error("Stripe error occurred: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCode());
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage());
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    private Long validateAndExtractAmount(Map<String, Object> payload) {
        try {
            Object amountObj = payload.get("amount");
            if (amountObj == null) {
                log.error("Amount is missing from payload");
                return null;
            }

            if (amountObj instanceof Integer) {
                return ((Integer) amountObj).longValue();
            } else if (amountObj instanceof Long) {
                return (Long) amountObj;
            } else if (amountObj instanceof Double) {
                return Math.round((Double) amountObj);
            } else if (amountObj instanceof String) {
                return Math.round(Double.parseDouble((String) amountObj));
            }

            log.error("Unsupported amount type: {}", amountObj.getClass());
            return null;
        } catch (NumberFormatException e) {
            log.error("Failed to parse amount: {}", e.getMessage());
            return null;
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
}

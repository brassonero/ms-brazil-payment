package mx.com.ebitware.stripe.payment.controller;

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
@RequestMapping("/api/payments")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://127.0.0.1:5173"},
        allowedHeaders = {"Content-Type", "Accept", "Authorization", "Origin"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class PaymentController {

    private static final String STRIPE_SECRET_KEY = "sk_test_51QeiWQQL0OOvl0KQX3CXSLCYeuY9o1lMgOOINRrTGdgRhfC0f7R2GmNhCPVIPL5f7O0WtndYS8SFrk0eHrM9DCOk00tZKkEHNB";
    private static final long DEFAULT_AMOUNT = 2000L; // $20.00

    public PaymentController() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        log.info("Received payment request");

        String stripeToken = (String) payload.get("stripeToken");
        if (stripeToken == null || stripeToken.isEmpty()) {
            log.error("Stripe token is missing");
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", "Stripe token is required"
                    ));
        }

        try {
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", DEFAULT_AMOUNT);
            chargeParams.put("currency", "usd");
            chargeParams.put("source", stripeToken);
            chargeParams.put("description", "Test Payment");

            log.info("Creating charge for token: {}", stripeToken);
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
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage(),
                            "code", e.getCode()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "An unexpected error occurred"
                    ));
        }
    }
}

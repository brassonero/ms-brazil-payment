package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.service.impl.WebSocketService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";
    private static final String ENDPOINT_SECRET = "whsec_af5bbf30a02e31915460727c22e140bb671b5364afbbe8a4f5e7ea1fc2cc2783";

    private final WebSocketService webSocketService;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(STRIPE_SIGNATURE_HEADER) String sigHeader) {
        try {
            log.info("📩 Payload recibido:\n{}", payload);

            Event event = Webhook.constructEvent(payload, sigHeader, ENDPOINT_SECRET);
            log.info("🔔 Evento recibido: {}", event.getType());

            webSocketService.sendPaymentEvent(payload);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                case "payment_intent.created":
                case "charge.succeeded":
                case "charge.updated":
                    log.info("✅ Procesando evento: {}", event.getType());
                    break;
                default:
                    log.warn("⚡ Evento no manejado: {}", event.getType());
            }

            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            log.error("⚠️ Firma no válida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("⚠️ Firma no válida: " + e.getMessage());
        } catch (Exception e) {
            log.error("⚠️ Error en webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("⚠️ Error en webhook: " + e.getMessage());
        }
    }

    // TODO: Remove mock
    @GetMapping("/{userId}/details")
    public ResponseEntity<Map<String, Object>> getSubscriptionDetails(@PathVariable String userId) {

        Map<String, Object> subscriptionDetails = new HashMap<>();

        Map<String, Object> currentPlan = new HashMap<>();
        currentPlan.put("planName", "Business Plan");
        currentPlan.put("extraAgent", "No");
        currentPlan.put("validUntil", "15/03/2022");
        currentPlan.put("renewal", "Monthly");
        currentPlan.put("status", "Active");

        Map<String, Object> conversationPackage = new HashMap<>();
        conversationPackage.put("usedConversations", 200);
        conversationPackage.put("totalConversations", 1000);
        conversationPackage.put("validUntil", "15/03/2022");
        conversationPackage.put("status", "Active");

        subscriptionDetails.put("currentPlan", currentPlan);
        subscriptionDetails.put("conversationPackage", conversationPackage);

        return ResponseEntity.ok(subscriptionDetails);
    }
}

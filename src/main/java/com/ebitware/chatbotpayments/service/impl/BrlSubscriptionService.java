package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.client.StripeClient;
import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.entity.BrlSubscription;
import com.ebitware.chatbotpayments.exception.CustomerNotFoundException;
import com.ebitware.chatbotpayments.exception.PriceNotFoundException;
import com.ebitware.chatbotpayments.exception.StripeApiException;
import com.ebitware.chatbotpayments.exception.SubscriptionException;
import com.ebitware.chatbotpayments.model.CreateSubscriptionRequest;
import com.ebitware.chatbotpayments.model.StripeSubscriptionResponse;
import com.ebitware.chatbotpayments.repository.billing.BrlCustomerRepository;
import com.ebitware.chatbotpayments.repository.billing.BrlPriceRepository;
import com.ebitware.chatbotpayments.repository.billing.BrlSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrlSubscriptionService {
    private final StripeClient stripeClient;
    private final BrlSubscriptionRepository brlSubscriptionRepository;
    private final BrlCustomerRepository brlCustomerRepository;
    private final BrlPriceRepository brlPriceRepository;

    public BrlSubscription createSubscription(CreateSubscriptionRequest request) {
        try {
            // Verify customer exists
            BrlCustomer customer = brlCustomerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + request.getCustomerId()));

            // Verify price exists
            BrlPrice price = brlPriceRepository.findById(request.getPriceId())
                    .orElseThrow(() -> new PriceNotFoundException("Price not found with id: " + request.getPriceId()));

            // Create parameters for Stripe
            Map<String, String> subscriptionDetails = new HashMap<>();
            subscriptionDetails.put("customer", customer.getStripeCustomerId());
            subscriptionDetails.put("items[0][price]", price.getStripePriceId());

            // Add metadata if present
            if (request.getMetadata() != null) {
                request.getMetadata().fields().forEachRemaining(entry ->
                        subscriptionDetails.put("metadata[" + entry.getKey() + "]", entry.getValue().asText())
                );
            }

            log.debug("Creating subscription in Stripe: {}", subscriptionDetails);
            StripeSubscriptionResponse stripeResponse = stripeClient.createSubscription(subscriptionDetails);
            log.debug("Received subscription response from Stripe: {}", stripeResponse);

            // Save to database
            BrlSubscription subscription = new BrlSubscription();
            subscription.setStripeSubscriptionId(stripeResponse.getId());
            subscription.setCustomerId(customer.getId());
            subscription.setStripeCustomerId(customer.getStripeCustomerId());
            subscription.setPriceId(price.getId());
            subscription.setStripePriceId(price.getStripePriceId());
            subscription.setStatus(stripeResponse.getStatus());
            subscription.setCurrentPeriodStart(
                    Instant.ofEpochSecond(stripeResponse.getCurrentPeriodStart()).atOffset(ZoneOffset.UTC)
            );
            subscription.setCurrentPeriodEnd(
                    Instant.ofEpochSecond(stripeResponse.getCurrentPeriodEnd()).atOffset(ZoneOffset.UTC)
            );
            subscription.setCancelAtPeriodEnd(stripeResponse.getCancelAtPeriodEnd());
            subscription.setMetadata(request.getMetadata());

            return brlSubscriptionRepository.save(subscription);

        } catch (StripeApiException e) {
            log.error("Stripe API error while creating subscription: {}", e.getMessage());
            throw new SubscriptionException("Error creating subscription in Stripe: " + e.getMessage(), e);
        } catch (CustomerNotFoundException | PriceNotFoundException e) {
            log.warn(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating subscription", e);
            throw new SubscriptionException("Unexpected error creating subscription", e);
        }
    }
}

package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.client.StripeClient;
import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.PriceException;
import com.ebitware.chatbotpayments.exception.ProductNotFoundException;
import com.ebitware.chatbotpayments.exception.StripeApiException;
import com.ebitware.chatbotpayments.model.CreatePriceRequest;
import com.ebitware.chatbotpayments.model.StripePriceResponse;
import com.ebitware.chatbotpayments.repository.billing.BrlPriceRepository;
import com.ebitware.chatbotpayments.repository.billing.BrlProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BrlPriceService {
    private final StripeClient stripeClient;
    private final BrlProductRepository brlProductRepository;
    private final BrlPriceRepository brlPriceRepository;

    public BrlPriceService(StripeClient stripeClient,
                           BrlProductRepository brlProductRepository,
                           BrlPriceRepository brlPriceRepository) {
        this.stripeClient = stripeClient;
        this.brlProductRepository = brlProductRepository;
        this.brlPriceRepository = brlPriceRepository;
    }

    public BrlPrice createPrice(CreatePriceRequest request) {
        try {
            // Verify product exists
            BrlProduct product = brlProductRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

            // Convert reais to cents for Stripe (e.g., 188000 reais = 18800000 cents)
            Long unitAmountInCents = request.getUnitAmount() * 100;

            log.debug("Converting price from {} reais to {} cents", request.getUnitAmount(), unitAmountInCents);

            // Create form-encoded parameters for Stripe
            Map<String, String> priceDetails = new HashMap<>();
            priceDetails.put("unit_amount", String.valueOf(unitAmountInCents));
            priceDetails.put("currency", request.getCurrency());
            priceDetails.put("recurring[interval]", request.getInterval());
            priceDetails.put("product", product.getStripeProductId());

            // Add metadata if present
            if (request.getMetadata() != null) {
                request.getMetadata().fields().forEachRemaining(entry ->
                        priceDetails.put("metadata[" + entry.getKey() + "]", entry.getValue().asText())
                );
            }

            log.debug("Creating price in Stripe for product {}: {}", product.getId(), priceDetails);
            StripePriceResponse stripeResponse = stripeClient.createPrice(priceDetails);
            log.debug("Received price response from Stripe: {}", stripeResponse);

            // Save to database - storing original amount in reais
            BrlPrice price = new BrlPrice();
            price.setStripePriceId(stripeResponse.getId());
            price.setProductId(product.getId());
            price.setStripeProductId(product.getStripeProductId());
            price.setUnitAmount(request.getUnitAmount());  // Store original amount in reais (188000)
            price.setCurrency(request.getCurrency());
            price.setInterval(request.getInterval());
            price.setActive(true);
            price.setMetadata(request.getMetadata());

            return brlPriceRepository.save(price);

        } catch (StripeApiException e) {
            log.error("Stripe API error while creating price: {}", e.getMessage());
            throw new PriceException("Error creating price in Stripe: " + e.getMessage(), e);
        } catch (ProductNotFoundException e) {
            log.warn("Product not found while creating price: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating price", e);
            throw new PriceException("Unexpected error creating price", e);
        }
    }
}

package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.entity.BrlPrice;
import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.PriceException;
import com.ebitware.chatbotpayments.exception.ProductNotFoundException;
import com.ebitware.chatbotpayments.model.CreatePriceRequest;
import com.ebitware.chatbotpayments.repository.billing.BrlPriceRepository;
import com.ebitware.chatbotpayments.repository.billing.BrlProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrlPriceService {

    private final StripeService stripeService;
    private final BrlProductRepository brlProductRepository;
    private final BrlPriceRepository brlPriceRepository;

    public BrlPrice createPrice(CreatePriceRequest request) {
        try {
            BrlProduct product = brlProductRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

            // Convert to cents while preserving decimals
            long unitAmountInCents = Math.round(request.getUnitAmount() * 100);
            log.debug("Converting price from {} reais to {} cents", request.getUnitAmount(), unitAmountInCents);

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
            Price stripePrice = stripeService.createPrice(priceDetails);
            log.debug("Created Stripe price: {}", stripePrice.getId());

            BrlPrice price = new BrlPrice();
            price.setStripePriceId(stripePrice.getId());
            price.setProductId(product.getId());
            price.setStripeProductId(product.getStripeProductId());
            price.setUnitAmount(request.getUnitAmount());  // Store original decimal amount
            price.setCurrency(request.getCurrency());
            price.setInterval(request.getInterval());
            price.setActive(true);
            price.setMetadata(request.getMetadata());

            return brlPriceRepository.save(price);

        } catch (StripeException e) {
            log.error("Stripe error while creating price: {}", e.getMessage());
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

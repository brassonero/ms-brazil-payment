package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.client.StripeClient;
import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.ProductException;
import com.ebitware.chatbotpayments.exception.StripeApiException;
import com.ebitware.chatbotpayments.model.CreateProductRequest;
import com.ebitware.chatbotpayments.model.StripeProductResponse;
import com.ebitware.chatbotpayments.repository.billing.BrlProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BrlProductService {
    private final StripeClient stripeClient;
    private final BrlProductRepository brlProductRepository;

    public BrlProductService(StripeClient stripeClient, BrlProductRepository brlProductRepository) {
        this.stripeClient = stripeClient;
        this.brlProductRepository = brlProductRepository;
    }

    public BrlProduct createProduct(CreateProductRequest request) {
        try {
            // Create form-encoded parameters for Stripe
            Map<String, String> productDetails = new HashMap<>();
            productDetails.put("name", request.getName());
            productDetails.put("description", request.getDescription());

            // Add metadata if present
            if (request.getMetadata() != null) {
                request.getMetadata().fields().forEachRemaining(entry ->
                        productDetails.put("metadata[" + entry.getKey() + "]", entry.getValue().asText())
                );
            }

            log.debug("Sending product creation request to Stripe: {}", productDetails);
            StripeProductResponse stripeResponse = stripeClient.createProduct(productDetails);
            log.debug("Received response from Stripe: {}", stripeResponse);

            // Save to local database
            BrlProduct product = new BrlProduct();
            product.setStripeProductId(stripeResponse.getId());
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setActive(true);
            product.setMetadata(request.getMetadata());

            return brlProductRepository.save(product);
        } catch (StripeApiException e) {
            log.error("Stripe API error while creating product: {}", e.getMessage());
            throw new ProductException("Error creating product in Stripe: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating product", e);
            throw new ProductException("Unexpected error creating product", e);
        }
    }
}

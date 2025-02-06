package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.entity.BrlProduct;
import com.ebitware.chatbotpayments.exception.ProductException;
import com.ebitware.chatbotpayments.model.CreateProductRequest;
import com.ebitware.chatbotpayments.repository.billing.BrlProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrlProductService {

    private final StripeService stripeService;
    private final BrlProductRepository brlProductRepository;

    public BrlProduct createProduct(CreateProductRequest request) {
        try {
            log.debug("Creating product in Stripe: {}", request);
            Product stripeProduct = stripeService.createProduct(request);
            log.debug("Created Stripe product: {}", stripeProduct.getId());

            BrlProduct product = new BrlProduct();
            product.setStripeProductId(stripeProduct.getId());
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setActive(true);
            product.setMetadata(request.getMetadata());

            return brlProductRepository.save(product);
        } catch (StripeException e) {
            log.error("Stripe error while creating product: {}", e.getMessage());
            throw new ProductException("Error creating product in Stripe: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating product", e);
            throw new ProductException("Unexpected error creating product", e);
        }
    }
}

package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.model.CreateProductRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.secret-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public Product createProduct(CreateProductRequest request) throws StripeException {
        try {
            ProductCreateParams.Builder paramsBuilder = ProductCreateParams.builder()
                    .setName(request.getName())
                    .setDescription(request.getDescription());

            if (request.getMetadata() != null) {
                request.getMetadata().fields().forEachRemaining(entry ->
                        paramsBuilder.putMetadata(entry.getKey(), entry.getValue().asText())
                );
            }

            return Product.create(paramsBuilder.build());
        } catch (StripeException e) {
            log.error("Error creating Stripe product: {}", e.getMessage());
            throw e;
        }
    }

    public Price createPrice(Map<String, String> priceDetails) throws StripeException {
        try {
            PriceCreateParams.Builder paramsBuilder = PriceCreateParams.builder()
                    .setUnitAmount(Long.parseLong(priceDetails.get("unit_amount")))
                    .setCurrency(priceDetails.get("currency"))
                    .setProduct(priceDetails.get("product"));

            if (priceDetails.containsKey("recurring[interval]")) {
                paramsBuilder.setRecurring(
                        PriceCreateParams.Recurring.builder()
                                .setInterval(PriceCreateParams.Recurring.Interval.valueOf(
                                        priceDetails.get("recurring[interval]").toUpperCase()
                                ))
                                .build()
                );
            }

            priceDetails.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("metadata["))
                    .forEach(entry -> {
                        String key = entry.getKey().substring(9, entry.getKey().length() - 1);
                        paramsBuilder.putMetadata(key, entry.getValue());
                    });

            return Price.create(paramsBuilder.build());
        } catch (StripeException e) {
            log.error("Error creating Stripe price: {}", e.getMessage());
            throw e;
        }
    }
}

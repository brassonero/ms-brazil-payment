package com.ebitware.chatbotpayments.client;

import com.ebitware.chatbotpayments.config.FeignConfig;
import com.ebitware.chatbotpayments.model.StripePriceResponse;
import com.ebitware.chatbotpayments.model.StripeProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;


@FeignClient(name = "stripeClient", url = "https://api.stripe.com/v1", configuration = FeignConfig.class)
public interface StripeClient {

    @PostMapping(value = "/prices", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    StripePriceResponse createPrice(@SpringQueryMap Map<String, String> priceDetails);

    @PostMapping(value = "/products", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    StripeProductResponse createProduct(@SpringQueryMap Map<String, String> productDetails);
}

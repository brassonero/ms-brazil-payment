package com.ebitware.chatbotpayments.config;

import com.ebitware.chatbotpayments.exception.CustomErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.http.MediaType;

@Configuration
public class FeignConfig {
    @Value("${stripe.secret-key}")
    private String stripeApiKey;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Encoder feignEncoder() {
        return new StripeFormEncoder();
    }

    @Bean
    public RequestInterceptor stripeAuthenticationInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + stripeApiKey);
            if (template.method().equals("POST")) {
                template.header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            }
        };
    }
}

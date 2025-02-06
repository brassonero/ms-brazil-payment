package com.ebitware.chatbotpayments.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            StripeErrorResponse errorResponse = objectMapper.readValue(bodyIs, StripeErrorResponse.class);

            return switch (response.status()) {
                case 400 -> new StripeApiException(
                        "Bad Request: " + errorResponse.getError().getMessage(),
                        HttpStatus.BAD_REQUEST,
                        errorResponse
                );
                case 401 -> new StripeApiException(
                        "Unauthorized: Invalid API key",
                        HttpStatus.UNAUTHORIZED,
                        errorResponse
                );
                case 404 -> new StripeApiException(
                        "Not Found: " + errorResponse.getError().getMessage(),
                        HttpStatus.NOT_FOUND,
                        errorResponse
                );
                case 429 -> new StripeApiException(
                        "Too Many Requests: Rate limit exceeded",
                        HttpStatus.TOO_MANY_REQUESTS,
                        errorResponse
                );
                default -> new StripeApiException(
                        "Stripe API Error: " + errorResponse.getError().getMessage(),
                        HttpStatus.valueOf(response.status()),
                        errorResponse
                );
            };
        } catch (IOException e) {
            log.error("Error decoding Stripe API error response", e);
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

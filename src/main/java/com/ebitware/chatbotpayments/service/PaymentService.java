package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;

import java.util.Map;

public interface PaymentService {

    Map<String, Object> processPayment(Map<String, Object> payload) throws PaymentValidationException, StripeException, JsonProcessingException;

    void validateAmount(Long amount) throws PaymentValidationException;
}

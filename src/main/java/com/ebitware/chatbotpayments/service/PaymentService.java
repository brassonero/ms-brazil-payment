package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    List<Map<String, Object>> listPaymentMethodsFormatted(String customerId)
            throws PaymentValidationException, StripeException;

    Map<String, Object> processPayment(Map<String, Object> payload) throws PaymentValidationException, StripeException, JsonProcessingException;

    void validateAmount(Long amount) throws PaymentValidationException;

    Map<String, Object> listPaymentMethods(String customerId) throws PaymentValidationException, StripeException;

    Map<String, Object> getSubscriptionById(String subscriptionId)
            throws PaymentValidationException, StripeException;

    Map<String, Object> cancelSubscription(String subscriptionId, boolean cancelImmediately)
            throws PaymentValidationException, StripeException;
}

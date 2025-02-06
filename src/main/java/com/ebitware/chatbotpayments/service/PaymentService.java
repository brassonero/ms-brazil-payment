package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.model.AddPaymentMethodRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.exception.StripeException;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    List<Map<String, Object>> listPaymentMethodsFormatted(String customerId)
            throws PaymentValidationException, StripeException;

    void validateAmount(Long amount) throws PaymentValidationException;

    Map<String, Object> processPayment(Map<String, Object> payload, Integer personId)
            throws PaymentValidationException, StripeException;

    List<Map<String, String>> getPaymentReceipts(String customerId, int page, int size) throws PaymentValidationException;

    String getPaymentReceipt(String paymentIntentId) throws PaymentValidationException, StripeException;

    Map<String, String> createSetupIntent(String customerId) throws PaymentValidationException, StripeException;
}

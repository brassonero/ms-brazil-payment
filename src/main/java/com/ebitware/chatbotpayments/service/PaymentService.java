package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.model.ChangeSubscriptionRequest;
import com.ebitware.chatbotpayments.model.TransactionDTO;
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

    String getSubscriptionReceipt(String subscriptionId) throws PaymentValidationException, StripeException;

    Map<String, Object> changeSubscription(String subscriptionId, ChangeSubscriptionRequest request)
            throws PaymentValidationException, StripeException;

    Map<String, String> getPlanDetails(String customerId) throws PaymentValidationException;

    void deletePaymentMethod(String userId, String paymentMethodId)
            throws PaymentValidationException, StripeException;

    Map<String, Object> getSubscriptionStatus(String userId)
            throws PaymentValidationException, StripeException;

    Map<String, Object> cancelSubscription(String userId, String subscriptionId, boolean cancelAtPeriodEnd)
            throws PaymentValidationException, StripeException;

    void updateDefaultPaymentMethod(String userId, String paymentMethodId)
            throws PaymentValidationException, StripeException;

    List<TransactionDTO> getCustomerTransactions(String customerId) throws StripeException;
}

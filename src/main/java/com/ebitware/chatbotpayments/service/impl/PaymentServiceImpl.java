package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final String currency;
    private final long minAmount;
    private final long maxAmount;

    public PaymentServiceImpl(
            @Value("${stripe.secret-key}") String stripeSecretKey,
            @Value("${payment.currency}") String currency,
            @Value("${payment.min-amount}") long minAmount,
            @Value("${payment.max-amount}") long maxAmount
    ) {
        this.currency = currency;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public Map<String, Object> processPayment(Map<String, Object> payload) throws PaymentValidationException, StripeException {

        String stripeToken = (String) payload.get("stripeToken");
        if (stripeToken == null || stripeToken.isEmpty()) {
            throw new PaymentValidationException("Stripe token is required");
        }

        Long amount = validateAndExtractAmount(payload);
        if (amount == null) {
            throw new PaymentValidationException("Invalid amount provided");
        }

        validateAmount(amount);

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", amount);
        chargeParams.put("currency", currency);
        chargeParams.put("source", stripeToken);
        chargeParams.put("description", "Dynamic Amount Payment");

        log.info("Creating charge for token: {} with amount: {}", stripeToken, amount);
        Charge charge = Charge.create(chargeParams);
        log.info("Charge created successfully: {}", charge.getId());

        return Map.of(
                "status", "success",
                "transactionId", charge.getId(),
                "amount", charge.getAmount(),
                "currency", charge.getCurrency(),
                "description", charge.getDescription()
        );
    }

    @Override
    public void validateAmount(Long amount) throws PaymentValidationException {
        if (amount < minAmount || amount > maxAmount) {
            throw new PaymentValidationException(
                    String.format("Amount must be between $%.2f and $%.2f", minAmount / 100.0, maxAmount / 100.0)
            );
        }
    }

    private Long validateAndExtractAmount(Map<String, Object> payload) {
        try {
            Object amountObj = payload.get("amount");
            if (amountObj == null) {
                log.error("Amount is missing from payload");
                return null;
            }

            if (amountObj instanceof Integer) {
                return ((Integer) amountObj).longValue();
            } else if (amountObj instanceof Long) {
                return (Long) amountObj;
            } else if (amountObj instanceof Double) {
                return Math.round((Double) amountObj);
            } else if (amountObj instanceof String) {
                return Math.round(Double.parseDouble((String) amountObj));
            }

            log.error("Unsupported amount type: {}", amountObj.getClass());
            return null;
        } catch (NumberFormatException e) {
            log.error("Failed to parse amount: {}", e.getMessage());
            return null;
        }
    }
}

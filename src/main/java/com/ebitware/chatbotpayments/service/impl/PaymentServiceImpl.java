package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
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
        log.info("Processing payment request");

        validateRequiredFields(payload);

        // Extract and clean data
        String paymentMethodId = (String) payload.get("paymentMethodId");
        String document = ((String) payload.get("document")).replaceAll("[^0-9]", "");
        String documentType = ((String) payload.get("documentType")).toUpperCase();
        String cardholderName = ((String) payload.get("cardholderName")).trim();
        Long amount = validateAndExtractAmount(payload);

        validateBrazilianFields(document, documentType, cardholderName);
        validateAmount(amount);

        try {
            // Create metadata for Brazilian requirements
            Map<String, String> metadata = createBrazilianMetadata(document, documentType, cardholderName);

            // Create customer
            Customer customer = createStripeCustomer(cardholderName, metadata);
            log.info("Created Stripe customer: {}", customer.getId());

            // Attach payment method to customer
            attachPaymentMethodToCustomer(paymentMethodId, customer.getId());
            log.info("Attached payment method to customer");

            // Create and confirm payment intent
            PaymentIntent paymentIntent = createPaymentIntent(
                    amount,
                    customer.getId(),
                    paymentMethodId,
                    metadata
            );
            log.info("Created payment intent: {}", paymentIntent.getId());

            // Handle payment status
            return handlePaymentStatus(paymentIntent, amount);

        } catch (StripeException e) {
            log.error("Stripe error processing payment: {}", e.getMessage());
            throw new PaymentValidationException("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    private Map<String, String> createBrazilianMetadata(String document, String documentType, String cardholderName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("tax_id_type", documentType);
        metadata.put("tax_id", document);
        metadata.put("document_type", documentType);
        metadata.put("customer_name", cardholderName);
        return metadata;
    }

    private Customer createStripeCustomer(String cardholderName, Map<String, String> metadata) throws StripeException {
        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setName(cardholderName)
                .setMetadata(metadata)
                .build();

        return Customer.create(customerParams);
    }

    private void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        paymentMethod.attach(PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build());
    }

    private PaymentIntent createPaymentIntent(
            Long amount,
            String customerId,
            String paymentMethodId,
            Map<String, String> metadata
    ) throws StripeException {
        PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency.toLowerCase())
                .setCustomer(customerId)
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setReturnUrl("http://localhost:5173/payment/success")
                .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION);

        // Add all metadata
        metadata.forEach(builder::putMetadata);

        return PaymentIntent.create(builder.build());
    }

    private Map<String, Object> handlePaymentStatus(PaymentIntent paymentIntent, Long amount)
            throws PaymentValidationException {
        String status = paymentIntent.getStatus();

        switch (status) {
            case "requires_action":
                return Map.of(
                        "status", "requires_action",
                        "client_secret", paymentIntent.getClientSecret(),
                        "next_action", paymentIntent.getNextAction()
                );

            case "succeeded":
                return Map.of(
                        "status", "success",
                        "transactionId", paymentIntent.getId(),
                        "amount", amount / 100.0,
                        "currency", currency,
                        "customerId", paymentIntent.getCustomer()
                );

            default:
                log.error("Unexpected payment status: {}", status);
                throw new PaymentValidationException("Status do pagamento inválido: " + status);
        }
    }

    private void validateRequiredFields(Map<String, Object> payload) throws PaymentValidationException {
        String[] requiredFields = {
                "paymentMethodId", "amount", "document", "documentType", "cardholderName"
        };

        for (String field : requiredFields) {
            if (!payload.containsKey(field) || payload.get(field) == null) {
                log.error("Missing required field: {}", field);
                throw new PaymentValidationException("Campo obrigatório ausente: " + field);
            }
        }
    }

    private void validateBrazilianFields(String document, String documentType, String cardholderName)
            throws PaymentValidationException {
        // Validate document type
        if (!documentType.equalsIgnoreCase("CPF") && !documentType.equalsIgnoreCase("CNPJ")) {
            log.error("Invalid document type: {}", documentType);
            throw new PaymentValidationException("Tipo de documento inválido");
        }

        // Validate document number
        boolean isValidDocument = documentType.equalsIgnoreCase("CPF")
                ? isValidCPF(document)
                : isValidCNPJ(document);

        if (!isValidDocument) {
            log.error("Invalid document number for type {}: {}", documentType, document);
            throw new PaymentValidationException(documentType + " inválido");
        }

        // Validate cardholder name
        if (cardholderName.length() < 3) {
            log.error("Invalid cardholder name: {}", cardholderName);
            throw new PaymentValidationException("Nome do titular do cartão inválido");
        }
    }

    private boolean isValidCPF(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int[] weights1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        // First digit verification
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * weights1[i];
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 >= 10) digit1 = 0;
        if (digit1 != Character.getNumericValue(cpf.charAt(9))) return false;

        // Second digit verification
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * weights2[i];
        }
        int digit2 = 11 - (sum % 11);
        if (digit2 >= 10) digit2 = 0;
        return digit2 == Character.getNumericValue(cpf.charAt(10));
    }

    private boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // First digit verification
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 >= 10) digit1 = 0;
        if (digit1 != Character.getNumericValue(cnpj.charAt(12))) return false;

        // Second digit verification
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        int digit2 = 11 - (sum % 11);
        if (digit2 >= 10) digit2 = 0;
        return digit2 == Character.getNumericValue(cnpj.charAt(13));
    }

    @Override
    public void validateAmount(Long amount) throws PaymentValidationException {
        if (amount < minAmount || amount > maxAmount) {
            log.error("Invalid amount: {}. Must be between {} and {}", amount, minAmount, maxAmount);
            throw new PaymentValidationException(
                    String.format("O valor deve estar entre R$%.2f e R$%.2f", minAmount / 100.0, maxAmount / 100.0)
            );
        }
    }

    private Long validateAndExtractAmount(Map<String, Object> payload) throws PaymentValidationException {
        try {
            Object amountObj = payload.get("amount");
            if (amountObj == null) {
                throw new PaymentValidationException("Valor é obrigatório");
            }

            Long amount;
            if (amountObj instanceof Integer) {
                amount = ((Integer) amountObj).longValue();
            } else if (amountObj instanceof Long) {
                amount = (Long) amountObj;
            } else if (amountObj instanceof Double) {
                amount = Math.round((Double) amountObj);
            } else if (amountObj instanceof String) {
                amount = Math.round(Double.parseDouble((String) amountObj));
            } else {
                log.error("Unsupported amount type: {}", amountObj.getClass());
                throw new PaymentValidationException("Formato de valor inválido");
            }

            validateAmount(amount);
            return amount;

        } catch (NumberFormatException e) {
            log.error("Failed to parse amount: {}", e.getMessage());
            throw new PaymentValidationException("Formato de valor inválido");
        }
    }
}
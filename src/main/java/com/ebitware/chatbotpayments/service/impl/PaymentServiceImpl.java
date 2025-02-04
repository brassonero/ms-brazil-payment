package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.entity.FormSubmission;
import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.repository.billing.BrlCustomerRepository;
import com.ebitware.chatbotpayments.repository.billing.BrlPaymentRepository;
import com.ebitware.chatbotpayments.repository.billing.BrlSubscriptionRepository;
import com.ebitware.chatbotpayments.repository.billing.FormSubmissionRepository;
import com.ebitware.chatbotpayments.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final String currency;
    private final long minAmount;
    private final long maxAmount;
    private final BrlCustomerRepository brlCustomerRepository;
    private final BrlPaymentRepository brlPaymentRepository;
    private final BrlSubscriptionRepository brlSubscriptionRepository;
    private final FormSubmissionRepository formSubmissionRepository;

    public PaymentServiceImpl(
            @Value("${stripe.secret-key}") String stripeSecretKey,
            @Value("${payment.currency}") String currency,
            @Value("${payment.min-amount}") long minAmount,
            @Value("${payment.max-amount}") long maxAmount,
            BrlCustomerRepository brlCustomerRepository,
            BrlPaymentRepository brlPaymentRepository,
            BrlSubscriptionRepository brlSubscriptionRepository, FormSubmissionRepository formSubmissionRepository
    ) {
        this.currency = currency;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.brlCustomerRepository = brlCustomerRepository;
        this.brlPaymentRepository = brlPaymentRepository;
        this.brlSubscriptionRepository = brlSubscriptionRepository;
        this.formSubmissionRepository = formSubmissionRepository;
        Stripe.apiKey = stripeSecretKey;
    }

    // TODO: AMEX

    @Override
    public List<Map<String, Object>> listPaymentMethodsFormatted(String customerId)
            throws PaymentValidationException, StripeException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId é obrigatório");
        }

        try {
            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(customerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();

            PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

            return paymentMethods.getData().stream()
                    .map(method -> {
                        PaymentMethod.Card card = method.getCard();
                        Map<String, Object> cardData = new HashMap<>();
                        cardData.put("id", method.getId());
                        cardData.put("cardType", formatCardType(card.getBrand()));
                        cardData.put("cardNumber", "**** **** **** " + card.getLast4());
                        PaymentMethod.BillingDetails billing = method.getBillingDetails();
                        cardData.put("cardholderName", billing != null ? billing.getName() : "");
                        cardData.put("bank", card.getIssuer() != null ? card.getIssuer().toUpperCase() : "");
                        cardData.put("isSelected", false);
                        return cardData;
                    })
                    .limit(3)
                    .collect(Collectors.toList());

        } catch (StripeException e) {
            log.error("Error retrieving payment methods: {}", e.getMessage());
            throw e;
        }
    }

    private String formatCardType(String brand) {
        if (brand == null || brand.isEmpty()) return "";
        return brand.substring(0, 1).toUpperCase() + brand.substring(1).toLowerCase();
    }

    @Override
    public Map<String, Object> processPayment(Map<String, Object> payload, Integer personId)
            throws PaymentValidationException, StripeException {
        log.info("Processing payment request for person ID: {}", personId);

        // Validate person exists in form submission
        Optional<FormSubmission> formSubmission = formSubmissionRepository.findByPersonId(personId);
        if (formSubmission.isEmpty()) {
            throw new PaymentValidationException("Invalid person_id");
        }

        validateRequiredFields(payload);

        String paymentMethodId = (String) payload.get("paymentMethodId");
        String document = ((String) payload.get("document")).replaceAll("[^0-9]", "");
        String documentType = ((String) payload.get("documentType")).toUpperCase();
        String cardholderName = ((String) payload.get("cardholderName")).trim();
        Boolean isSubscription = (Boolean) payload.getOrDefault("isSubscription", false);

        validateBrazilianFields(document, documentType, cardholderName);

        try {
            String customerId = getOrCreateCustomer(document, documentType, cardholderName, personId);
            log.info("Using customer: {} for person: {}", customerId, personId);

            attachPaymentMethodToCustomer(paymentMethodId, customerId);
            log.info("Attached payment method to customer");

            if (Boolean.TRUE.equals(isSubscription)) {
                return processSubscriptionPayment(customerId, paymentMethodId, payload);
            } else {
                return processOneTimePayment(customerId, paymentMethodId, payload);
            }
        } catch (StripeException e) {
            log.error("Stripe error processing payment: {}", e.getMessage());
            throw new PaymentValidationException("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    private String getOrCreateCustomer(String document, String documentType,
                                       String cardholderName, Integer personId)
            throws StripeException {
        Optional<BrlCustomer> existingCustomer =
                brlCustomerRepository.findByDocumentAndDocumentType(document, documentType);

        if (existingCustomer.isPresent()) {
            log.info("Found existing customer with document {} and type {}",
                    document, documentType);
            return existingCustomer.get().getId();
        }

        log.info("Creating new customer for document {} and type {}",
                document, documentType);
        CustomerCreateParams.Builder customerParamsBuilder = CustomerCreateParams.builder()
                .setName(cardholderName)
                .putMetadata("tax_id_type", documentType)
                .putMetadata("tax_id", document)
                .putMetadata("document_type", documentType)
                .putMetadata("customer_name", cardholderName)
                .putMetadata("person_id", personId.toString());

        Customer customer = Customer.create(customerParamsBuilder.build());

        brlCustomerRepository.save(
                customer.getId(),
                document,
                documentType,
                cardholderName,
                customer.getMetadata(),
                personId
        );

        return customer.getId();
    }

    private void attachPaymentMethodToCustomer(String paymentMethodId, String customerId)
            throws StripeException {
        try {
            PaymentMethod newPaymentMethod = PaymentMethod.retrieve(paymentMethodId);
            String newFingerprint = newPaymentMethod.getCard().getFingerprint();

            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(customerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();

            PaymentMethodCollection existingMethods = PaymentMethod.list(params);

            // Check for existing card with same fingerprint (exact same card)
            for (PaymentMethod existingMethod : existingMethods.getData()) {
                if (existingMethod.getCard().getFingerprint().equals(newFingerprint)) {
                    existingMethod.detach();
                    log.info("Detached duplicate payment method: {}", existingMethod.getId());
                    break;
                }
            }

            if (newPaymentMethod.getCustomer() != null) {
                newPaymentMethod.detach();
                newPaymentMethod = PaymentMethod.retrieve(paymentMethodId);
            }

            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build();
            newPaymentMethod.attach(attachParams);

            log.info("Payment method attached successfully to customer: {}", customerId);
        } catch (StripeException e) {
            log.error("Error attaching payment method to customer: {}", e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> processOneTimePayment(
            String customerId,
            String paymentMethodId,
            Map<String, Object> payload
    ) throws PaymentValidationException, StripeException {
        Long amount = validateAndExtractAmount(payload);
        validateAmount(amount);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency.toLowerCase())
                    .setCustomer(customerId)
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .setReturnUrl("http://localhost:5173/payment/success")
                    .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            log.info("Created payment intent: {}", paymentIntent.getId());

            brlPaymentRepository.save(
                    paymentIntent.getId(),
                    customerId,
                    paymentMethodId,
                    "one_time",
                    currency,
                    BigDecimal.valueOf(amount, 2),
                    paymentIntent.getStatus(),
                    paymentIntent.getMetadata()
            );

            return handleOneTimePaymentStatus(paymentIntent, amount);
        } catch (StripeException e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> processSubscriptionPayment(
            String customerId,
            String paymentMethodId,
            Map<String, Object> payload
    ) throws PaymentValidationException {
        try {
            String priceId = (String) payload.get("priceId");
            if (priceId == null || priceId.isEmpty()) {
                throw new PaymentValidationException("priceId é obrigatório para assinaturas");
            }

            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .setDefaultPaymentMethod(paymentMethodId)
                    .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                    .build();

            Subscription subscription = Subscription.create(params);

            brlSubscriptionRepository.save(
                    subscription.getId(),
                    customerId,
                    subscription.getStatus(),
                    priceId,
                    subscription.getCurrency(),
                    paymentMethodId,
                    subscription.getMetadata()
            );

            return handleSubscriptionStatus(subscription);

        } catch (StripeException e) {
            log.error("Stripe subscription error: {}", e.getMessage());
            throw new PaymentValidationException("Erro ao criar assinatura: " + e.getMessage());
        }
    }

    private Map<String, Object> handleOneTimePaymentStatus(PaymentIntent paymentIntent, Long amount)
            throws PaymentValidationException {
        String status = paymentIntent.getStatus();

        return switch (status) {
            case "requires_action" -> Map.of(
                    "status", "requires_action",
                    "client_secret", paymentIntent.getClientSecret(),
                    "next_action", paymentIntent.getNextAction()
            );
            case "succeeded" -> Map.of(
                    "status", "success",
                    "transactionId", paymentIntent.getId(),
                    "amount", amount / 100.0,
                    "currency", currency,
                    "customerId", paymentIntent.getCustomer(),
                    "paymentType", "one_time"
            );
            default -> {
                log.error("Unexpected payment status: {}", status);
                throw new PaymentValidationException("Status do pagamento inválido: " + status);
            }
        };
    }

    private Map<String, Object> handleSubscriptionStatus(Subscription subscription)
            throws PaymentValidationException {
        String status = subscription.getStatus();

        if ("active".equals(status) || "trialing".equals(status)) {
            return Map.of(
                    "status", "success",
                    "subscriptionId", subscription.getId(),
                    "customerId", subscription.getCustomer(),
                    "currency", subscription.getCurrency(),
                    "subscriptionStatus", status,
                    "paymentType", "subscription"
            );
        } else {
            log.error("Unexpected subscription status: {}", status);
            throw new PaymentValidationException("Status da assinatura inválido: " + status);
        }
    }

    @Override
    public Map<String, Object> getSubscriptionById(String subscriptionId)
            throws PaymentValidationException, StripeException {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new PaymentValidationException("SubscriptionId é obrigatório");
        }

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Map<String, Object> response = new HashMap<>();
            response.put("id", subscription.getId());
            response.put("status", subscription.getStatus());
            response.put("customer_id", subscription.getCustomer());
            response.put("currency", subscription.getCurrency());
            response.put("current_period_end", subscription.getCurrentPeriodEnd());
            response.put("cancel_at_period_end", subscription.getCancelAtPeriodEnd());

            if (subscription.getCanceledAt() != null) {
                response.put("canceled_at", subscription.getCanceledAt());
            }

            return response;
        } catch (StripeException e) {
            log.error("Error retrieving subscription: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, Object> cancelSubscription(String subscriptionId, boolean cancelImmediately)
            throws PaymentValidationException, StripeException {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new PaymentValidationException("SubscriptionId é obrigatório");
        }

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Subscription canceledSubscription;

            if (cancelImmediately) {
                canceledSubscription = subscription.cancel();
            } else {
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                canceledSubscription = subscription.update(params);
            }

            Optional<BrlCustomer> customerOpt = brlCustomerRepository.findById(canceledSubscription.getCustomer());

            if (customerOpt.isPresent()) {
                brlSubscriptionRepository.save(
                        canceledSubscription.getId(),
                        canceledSubscription.getCustomer(),
                        canceledSubscription.getStatus(),
                        canceledSubscription.getItems().getData().get(0).getPrice().getId(),
                        canceledSubscription.getCurrency(),
                        canceledSubscription.getDefaultPaymentMethod(),
                        canceledSubscription.getMetadata()
                );
            }

            return Map.of(
                    "subscription_id", canceledSubscription.getId(),
                    "status", canceledSubscription.getStatus(),
                    "cancel_at_period_end", canceledSubscription.getCancelAtPeriodEnd(),
                    "current_period_end", canceledSubscription.getCurrentPeriodEnd()
            );

        } catch (StripeException e) {
            log.error("Error canceling subscription: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void validateAmount(Long amount) throws PaymentValidationException {
        if (amount < minAmount || amount > maxAmount) {
            log.error("Invalid amount: {}. Must be between {} and {}", amount, minAmount, maxAmount);
            throw new PaymentValidationException(
                    String.format("O valor deve estar entre R$%.2f e R$%.2f",
                            minAmount / 100.0, maxAmount / 100.0)
            );
        }
    }

    private void validateRequiredFields(Map<String, Object> payload) throws PaymentValidationException {
        Boolean isSubscription = (Boolean) payload.getOrDefault("isSubscription", false);

        String[] requiredFields = Boolean.TRUE.equals(isSubscription)
                ? new String[]{"paymentMethodId", "document", "documentType", "cardholderName", "priceId"}
                : new String[]{"paymentMethodId", "amount", "document", "documentType", "cardholderName"};

        for (String field : requiredFields) {
            if (!payload.containsKey(field) || payload.get(field) == null) {
                log.error("Missing required field: {}", field);
                throw new PaymentValidationException("Campo obrigatório ausente: " + field);
            }
        }

        if (Boolean.TRUE.equals(isSubscription)) {
            String priceId = (String) payload.get("priceId");
            if (priceId.trim().isEmpty()) {
                throw new PaymentValidationException("priceId não pode estar vazio");
            }
        }
    }

    private void validateBrazilianFields(String document, String documentType, String cardholderName)
            throws PaymentValidationException {
        if (!documentType.equalsIgnoreCase("CPF") && !documentType.equalsIgnoreCase("CNPJ")) {
            log.error("Invalid document type: {}", documentType);
            throw new PaymentValidationException("Tipo de documento inválido");
        }

        boolean isValidDocument = documentType.equalsIgnoreCase("CPF")
                ? isValidCPF(document)
                : isValidCNPJ(document);

        if (!isValidDocument) {
            log.error("Invalid document number for type {}: {}", documentType, document);
            throw new PaymentValidationException(documentType + " inválido");
        }

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

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * weights1[i];
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 >= 10) digit1 = 0;
        if (digit1 != Character.getNumericValue(cpf.charAt(9))) return false;

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

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 >= 10) digit1 = 0;
        if (digit1 != Character.getNumericValue(cnpj.charAt(12))) return false;

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        int digit2 = 11 - (sum % 11);
        if (digit2 >= 10) digit2 = 0;
        return digit2 == Character.getNumericValue(cnpj.charAt(13));
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

    public Map<String, Object> listPaymentMethods(String customerId) throws PaymentValidationException, StripeException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId é obrigatório");
        }

        try {
            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(customerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();

            PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
            List<Map<String, Object>> formattedMethods = new ArrayList<>();

            for (PaymentMethod method : paymentMethods.getData()) {
                PaymentMethod.Card card = method.getCard();
                if (card != null) {
                    Map<String, Object> cardData = new HashMap<>();
                    cardData.put("id", method.getId());
                    cardData.put("cardType", card.getBrand());
                    cardData.put("cardNumber", "**** **** **** " + card.getLast4());
                    cardData.put("expiryMonth", card.getExpMonth());
                    cardData.put("expiryYear", card.getExpYear());
                    cardData.put("isDefault", method.getId().equals(getDefaultPaymentMethod(customerId)));

                    PaymentMethod.BillingDetails billing = method.getBillingDetails();
                    if (billing != null && billing.getName() != null) {
                        cardData.put("cardholderName", billing.getName());
                    }

                    formattedMethods.add(cardData);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("customer_id", customerId);
            response.put("payment_methods", formattedMethods);

            return response;
        } catch (StripeException e) {
            log.error("Error retrieving payment methods: {}", e.getMessage());
            throw e;
        }
    }

    private String getDefaultPaymentMethod(String customerId) throws StripeException {
        Customer customer = Customer.retrieve(customerId);
        return customer.getInvoiceSettings() != null ?
                customer.getInvoiceSettings().getDefaultPaymentMethod() : null;
    }
}

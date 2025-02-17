package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.entity.BrlSubscription;
import com.ebitware.chatbotpayments.entity.FormSubmission;
import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.model.ChangeSubscriptionRequest;
import com.ebitware.chatbotpayments.model.PaymentSuccessEvent;
import com.ebitware.chatbotpayments.model.TransactionDTO;
import com.ebitware.chatbotpayments.repository.billing.*;
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
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final String growthMonthly;
    private final String growthAnnual;
    private final String businessMonthly;
    private final String businessAnnual;
    private final String enterpriseMonthly;
    private final String enterpriseAnnual;
    private final String currency;
    private final long minAmount;
    private final long maxAmount;
    private final BrlCustomerRepository brlCustomerRepository;
    private final BrlPaymentRepository brlPaymentRepository;
    private final BrlSubscriptionRepository brlSubscriptionRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final PaymentSuccessEmailService emailService;

    public PaymentServiceImpl(
            @Value("${spring.config.stripe.prices.growth.monthly}") String growthMonthly,
            @Value("${spring.config.stripe.prices.growth.annual}") String growthAnnual,
            @Value("${spring.config.stripe.prices.business.monthly}") String businessMonthly,
            @Value("${spring.config.stripe.prices.business.annual}") String businessAnnual,
            @Value("${spring.config.stripe.prices.enterprise.monthly}") String enterpriseMonthly,
            @Value("${spring.config.stripe.prices.enterprise.annual}") String enterpriseAnnual,
            @Value("${stripe.secret-key}") String stripeSecretKey,
            @Value("${payment.currency}") String currency,
            @Value("${payment.min-amount}") long minAmount,
            @Value("${payment.max-amount}") long maxAmount,
            BrlCustomerRepository brlCustomerRepository,
            BrlPaymentRepository brlPaymentRepository,
            BrlSubscriptionRepository brlSubscriptionRepository, FormSubmissionRepository formSubmissionRepository, PaymentSuccessEmailService emailService
    ) {
        this.growthMonthly = growthMonthly;
        this.growthAnnual = growthAnnual;
        this.businessMonthly = businessMonthly;
        this.businessAnnual = businessAnnual;
        this.enterpriseMonthly = enterpriseMonthly;
        this.enterpriseAnnual = enterpriseAnnual;
        this.currency = currency;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.brlCustomerRepository = brlCustomerRepository;
        this.brlPaymentRepository = brlPaymentRepository;
        this.brlSubscriptionRepository = brlSubscriptionRepository;
        this.formSubmissionRepository = formSubmissionRepository;
        this.emailService = emailService;
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public List<Map<String, Object>> listPaymentMethodsFormatted(String customerId)
            throws PaymentValidationException, StripeException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId é obrigatório");
        }

        try {
            // Retrieve the customer to check the default payment method
            Customer customer = Customer.retrieve(customerId);
            String defaultPaymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();

            // Retrieve the payment methods
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

                        boolean isDefault = method.getId().equals(defaultPaymentMethodId);
                        cardData.put("isSelected", isDefault);

                        return cardData;
                    })
                    .limit(3)
                    .collect(Collectors.toList());

        } catch (StripeException e) {
            log.error("Error retrieving payment methods: {}", e.getMessage());
            throw e;
        }
    }

    public List<Map<String, Object>> listPaymentMethodsFormattedForSubscription(String customerId, String subscriptionId)
            throws PaymentValidationException, StripeException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId é obrigatório");
        }
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new PaymentValidationException("SubscriptionId é obrigatório");
        }

        try {
            // Retrieve the subscription to check the default payment method
            Subscription subscription = Subscription.retrieve(subscriptionId);
            String defaultPaymentMethodId = subscription.getDefaultPaymentMethod();

            // Retrieve the payment methods
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
/*
                        boolean isDefault = method.getId().equals(defaultPaymentMethodId);
                        cardData.put("isSelected", isDefault);
 */

                        boolean isDefault = method.getId().equals(defaultPaymentMethodId);
                        cardData.put("isSelected", isDefault);

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
            throws PaymentValidationException {
        log.info("Processing payment request for person ID: {}", personId);

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
            BrlCustomer customer = existingCustomer.get();
            log.info("Found existing customer with document {} and type {}",
                    document, documentType);

            Map<String, String> updatedMetadata = new HashMap<>(customer.getMetadata());
            updatedMetadata.put("person_id", personId.toString());

            brlCustomerRepository.save(
                    customer.getId(),
                    document,
                    documentType,
                    cardholderName,
                    updatedMetadata,
                    personId
            );
            log.info("Updated customer {} with new personId {}", customer.getId(), personId);

            return customer.getId();
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

            if ("succeeded".equals(paymentIntent.getStatus())) {
                sendSuccessEmails(payload, customerId, amount);
            }

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
            // TODO: Inspect
            if ("active".equals(subscription.getStatus()) || "trialing".equals(subscription.getStatus())) {
                Price price = Price.retrieve(priceId);
                sendSuccessEmails(payload, customerId, price.getUnitAmount());
            }

            return handleSubscriptionStatus(subscription);

        } catch (StripeException e) {
            log.error("Stripe subscription error: {}", e.getMessage());
            throw new PaymentValidationException("Erro ao criar assinatura: " + e.getMessage());
        }
    }

    private void sendSuccessEmails(Map<String, Object> payload, String customerId, Long amount) {
        try {
            Integer personId = parsePersonId(payload.get("personId"));
            FormSubmission form = formSubmissionRepository.findByPersonId(personId)
                    .orElseThrow(() -> new PaymentValidationException("Form submission not found for person ID: " + personId));

            boolean isSubscription = Boolean.TRUE.equals(payload.getOrDefault("isSubscription", false));
            String planName;
            String contractPeriod;
            String monthlyVolume;

            if (isSubscription) {
                String priceId = (String) payload.get("priceId");
                if (priceId == null || priceId.isBlank()) {
                    throw new PaymentValidationException("Price ID is required for subscriptions");
                }
                planName = determinePlanFromPriceId(priceId);
                contractPeriod = determineContractPeriodFromPriceId(priceId);
                monthlyVolume = "N/A";
            } else {
                planName = "Growth";
                contractPeriod = "One-Time";
                monthlyVolume = determineMonthlyVolume(amount);
            }

            String setupFee = determineSetupFee(contractPeriod);

            PaymentSuccessEvent event = buildPaymentSuccessEvent(payload, form, planName, contractPeriod, monthlyVolume, amount, setupFee);

            emailService.sendPaymentSuccessEmails(event);
            log.info("Success emails sent for payment from customer: {} with plan: {}", customerId, planName);
        } catch (NumberFormatException e) {
            log.error("Invalid personId format: {}", e.getMessage());
            throw new RuntimeException("Invalid personId format", e);
        } catch (PaymentValidationException | RuntimeException e) {
            log.error("Error sending success emails: {}", e.getMessage());
            throw new RuntimeException("Failed to send success emails", e);
        }
    }

    private PaymentSuccessEvent buildPaymentSuccessEvent(Map<String, Object> payload, FormSubmission form, String planName, String contractPeriod, String monthlyVolume, Long amount, String setupFee) {
        String currency = Optional.ofNullable(this.currency).orElse("BRL").toUpperCase();

        return PaymentSuccessEvent.builder()
                .companyName(form.getBusinessName())
                .bmId(form.getFacebookManagerNo())
                .commercialName(form.getDisplayName())
                .phone(form.getPhone())
                .email(form.getCorporateEmail())
                .website(form.getWebsite())
                .address(form.getAddress())
                .vertical(form.getVertical())
                .businessDescription(form.getDescription())
                .planName(planName)
                .planValue(formatAmount(amount))
                .currency(currency)
                .contractPeriod(contractPeriod)
                .startDate(formatDate(LocalDate.now()))
                .endDate(contractPeriod.equals("One-Time") ? "N/A" : formatDate(determineEndDate(contractPeriod)))
                .agents(contractPeriod.equals("One-Time") ? "N/A" : String.valueOf(getAgentsForPlan(planName)))
                .addons((String) payload.getOrDefault("addons", "Basic"))
                .monthlyVolume(monthlyVolume)
                .channels((String) payload.getOrDefault("channels", "WhatsApp"))
                .paymentDate(formatDateTime(LocalDateTime.now()))
                .setupFee(contractPeriod.equals("One-Time") ? "N/A" : setupFee)
                .customerEmail(form.getCorporateEmail())
                .build();
    }

    private String determinePlanFromPriceId(String priceId) {
        if (priceId.equals(growthMonthly) || priceId.equals(growthAnnual)) {
            return "Growth";
        } else if (priceId.equals(businessMonthly) || priceId.equals(businessAnnual)) {
            return "Business";
        } else if (priceId.equals(enterpriseMonthly) || priceId.equals(enterpriseAnnual)) {
            return "Enterprise";
        }
        throw new RuntimeException("Invalid price ID: " + priceId);
    }

    private String determineContractPeriodFromPriceId(String priceId) {
        if (priceId.equals(growthAnnual) ||
                priceId.equals(businessAnnual) ||
                priceId.equals(enterpriseAnnual)) {
            return "Anual";
        }
        return "Mensal";
    }

    private LocalDate determineEndDate(String contractPeriod) {
        return contractPeriod.equals("Anual") ? LocalDate.now().plusYears(1) : LocalDate.now().plusMonths(1);
    }

    private String determineSetupFee(String contractPeriod) {
        return contractPeriod.equals("Anual") ? "R$149,83" : "R$179,80";
    }

    private String determineMonthlyVolume(Long amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null for one-time payments");
        }

        if (amount == 17980L) {
            return "Setup Fee Mensal";
        } else if (amount == 14983L) {
            return "Setup Fee Anual";
        } else if (amount <= 495000L) {
            return "5.000 Conversas";
        } else if (amount <= 980000L) {
            return "10.000 Conversas";
        } else if (amount <= 2910000L) {
            return "30.000 Conversas";
        } else if (amount <= 4800000L) {
            return "50.000 Conversas";
        } else if (amount <= 9500000L) {
            return "100.000 Conversas";
        } else if (amount <= 18800000L) {
            return "200.000 Conversas";
        } else {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private Integer parsePersonId(Object personIdObj) {
        if (personIdObj == null) {
            throw new RuntimeException("personId is required");
        }
        try {
            return Integer.parseInt(personIdObj.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid personId format: " + personIdObj, e);
        }
    }

    private int getAgentsForPlan(String planName) {
        if (planName == null) {
            throw new RuntimeException("Plan name cannot be null");
        }
        return switch (planName.toLowerCase()) {
            case "growth" -> 5;
            case "business" -> 10;
            case "enterprise" -> 15;
            default -> throw new RuntimeException("Invalid plan name: " + planName);
        };
    }

    private String formatAmount(Long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        formatter.setCurrency(Currency.getInstance(currency.toUpperCase()));
        return formatter.format(amount / 100.0);
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

    @Override
    public List<Map<String, String>> getPaymentReceipts(String customerId, int page, int size)
            throws PaymentValidationException {
        if (customerId != null && customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId cannot be empty if provided");
        }

        try {
            List<Map<String, Object>> payments = brlPaymentRepository.findPaymentReceipts(customerId, page, size);

            return payments.stream()
                    .map(payment -> {
                        Map<String, String> receipt = new HashMap<>();

                        LocalDateTime dateTime = ((Timestamp) payment.get("created_at")).toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                        receipt.put("date", dateTime.format(formatter));

                        BigDecimal amount = (BigDecimal) payment.get("amount");
                        String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("en", "US"))
                                .format(amount);
                        receipt.put("totalAmount", formattedAmount);

                        String paymentId = (String) payment.get("id");
                        receipt.put("paymentId", paymentId);

                        String receiptNumber = paymentId.substring(Math.max(0, paymentId.length() - 8))
                                .toUpperCase();
                        receipt.put("receiptNumber", receiptNumber);

                        return receipt;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving payment receipts", e);
            throw new PaymentValidationException("Error retrieving payment receipts");
        }
    }

    @Override
    public String getPaymentReceipt(String paymentIntentId) throws PaymentValidationException, StripeException {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new PaymentValidationException("Payment ID is required");
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                throw new PaymentValidationException("Receipt is only available for successful payments");
            }

            String chargeId = paymentIntent.getLatestCharge();
            if (chargeId == null) {
                throw new PaymentValidationException("No charge found for this payment");
            }

            Charge charge = Charge.retrieve(chargeId);
            return charge.getReceiptUrl();

        } catch (StripeException e) {
            log.error("Error retrieving Stripe receipt: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, String> createSetupIntent(String customerId) throws PaymentValidationException, StripeException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId é obrigatório");
        }

        SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                .setCustomer(customerId)
                .addPaymentMethodType("card")
                .build();

        SetupIntent setupIntent = SetupIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", setupIntent.getClientSecret());
        return response;
    }

    @Override
    public String getSubscriptionReceipt(String subscriptionId) throws PaymentValidationException, StripeException {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new PaymentValidationException("Subscription ID is required");
        }

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);

            Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());

            if (!"paid".equals(invoice.getStatus())) {
                throw new PaymentValidationException("Receipt is only available for paid invoices");
            }

            String chargeId = invoice.getCharge();
            if (chargeId == null) {
                throw new PaymentValidationException("No charge found for this subscription invoice");
            }

            Charge charge = Charge.retrieve(chargeId);
            String receiptUrl = charge.getReceiptUrl();

            if (receiptUrl == null) {
                throw new PaymentValidationException("No receipt available for this subscription payment");
            }

            return receiptUrl;
        } catch (StripeException e) {
            log.error("Error retrieving Stripe subscription receipt: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, Object> changeSubscription(String subscriptionId, ChangeSubscriptionRequest request)
            throws PaymentValidationException, StripeException {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new PaymentValidationException("Subscription ID is required");
        }

        try {
            Subscription existingSubscription = Subscription.retrieve(subscriptionId);

            if (!"active".equals(existingSubscription.getStatus())
                    && !"trialing".equals(existingSubscription.getStatus())) {
                throw new PaymentValidationException(
                        "Cannot change subscription that is not active or trialing");
            }

            String subscriptionItemId = existingSubscription.getItems().getData().get(0).getId();

            Map<String, Object> params = new HashMap<>();

            Map<String, Object> itemParams = new HashMap<>();
            itemParams.put("id", subscriptionItemId);
            itemParams.put("price", request.getNewPriceId());

            if (!request.isProrationBehavior()) {
                params.put("proration_behavior", "none");
            }

            List<Object> items = new ArrayList<>();
            items.add(itemParams);
            params.put("items", items);

            Subscription updatedSubscription = existingSubscription.update(params);

            String paymentMethodId = updatedSubscription.getDefaultPaymentMethod();
            if (paymentMethodId == null) {
                BrlSubscription currentSubscription = brlSubscriptionRepository
                        .findById(subscriptionId)
                        .orElseThrow(() -> new PaymentValidationException("Subscription not found in database"));
                paymentMethodId = currentSubscription.getPaymentMethodId();
            }

            brlSubscriptionRepository.updateSubscription(
                    updatedSubscription.getId(),
                    updatedSubscription.getStatus(),
                    request.getNewPriceId(),
                    paymentMethodId,
                    updatedSubscription.getMetadata()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("subscriptionId", updatedSubscription.getId());
            response.put("newPriceId", request.getNewPriceId());
            response.put("subscriptionStatus", updatedSubscription.getStatus());
            response.put("paymentMethodId", paymentMethodId);

            if (request.isProrationBehavior()) {
                Invoice invoice = Invoice.retrieve(updatedSubscription.getLatestInvoice());
                response.put("prorationAmount", invoice.getAmountDue());
                response.put("invoiceId", invoice.getId());
            }

            return response;

        } catch (StripeException e) {
            log.error("Error changing subscription: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, String> getPlanDetails(String customerId) throws PaymentValidationException {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentValidationException("CustomerId é obrigatório");
        }

        Map<String, Object> planDetails = brlSubscriptionRepository.findPlanDetails(customerId)
                .orElseThrow(() -> new PaymentValidationException("No active subscription found"));

        Map<String, String> response = new HashMap<>();
        response.put("extraAgent", String.valueOf(planDetails.getOrDefault("extra_agent", "No")));
        response.put("renewal", planDetails.get("interval_type").equals("month") ? "Monthly" : "Yearly");
        response.put("planName", (String) planDetails.get("plan_name"));

        LocalDateTime startDate = ((Timestamp) planDetails.get("start_date")).toLocalDateTime();
        LocalDateTime validUntil = planDetails.get("interval_type").equals("month")
                ? startDate.plusMonths(1)
                : startDate.plusYears(1);
        response.put("validUntil", validUntil.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        response.put("status", planDetails.get("subscription_status").equals("active") ? "Active" : "Inactive");

        return response;
    }

    @Override
    public void deletePaymentMethod(String userId, String paymentMethodId)
            throws PaymentValidationException, StripeException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("UserId é obrigatório");
        }
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new PaymentValidationException("PaymentMethodId é obrigatório");
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            if (!userId.equals(paymentMethod.getCustomer())) {
                throw new PaymentValidationException("Payment method does not belong to this user");
            }

            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(userId)
                    .setStatus(SubscriptionListParams.Status.ACTIVE)
                    .build();

            SubscriptionCollection subscriptions = Subscription.list(params);
            for (Subscription subscription : subscriptions.getData()) {
                if (paymentMethodId.equals(subscription.getDefaultPaymentMethod())) {
                    throw new PaymentValidationException(
                            "Cannot delete default payment method for active subscription");
                }
            }

            paymentMethod.detach();
            log.info("Successfully deleted payment method {} for user {}", paymentMethodId, userId);

        } catch (StripeException e) {
            log.error("Error deleting payment method: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<String, Object> getSubscriptionStatus(String userId)
            throws PaymentValidationException, StripeException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("UserId é obrigatório");
        }

        SubscriptionListParams params = SubscriptionListParams.builder()
                .setCustomer(userId)
                .setLimit(1L)
                .build();

        SubscriptionCollection subscriptions = Subscription.list(params);

        if (subscriptions.getData().isEmpty()) {
            return Map.of(
                    "hasSubscription", false,
                    "status", "none"
            );
        }

        Subscription subscription = subscriptions.getData().get(0);
        return Map.of(
                "hasSubscription", true,
                "status", subscription.getStatus(),
                "subscriptionId", subscription.getId(),
                "currentPeriodEnd", subscription.getCurrentPeriodEnd(),
                "cancelAtPeriodEnd", subscription.getCancelAtPeriodEnd()
        );
    }

    @Override
    public Map<String, Object> cancelSubscription(String userId, String subscriptionId, boolean cancelAtPeriodEnd)
            throws PaymentValidationException, StripeException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("UserId é obrigatório");
        }
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new PaymentValidationException("SubscriptionId é obrigatório");
        }

        Subscription subscription = Subscription.retrieve(subscriptionId);

        if (!userId.equals(subscription.getCustomer())) {
            throw new PaymentValidationException("Subscription does not belong to this user");
        }

        if (cancelAtPeriodEnd) {
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            subscription = subscription.update(params);
        } else {
            subscription = subscription.cancel();
        }

        return Map.of(
                "status", subscription.getStatus(),
                "cancelAtPeriodEnd", subscription.getCancelAtPeriodEnd(),
                "endedAt", subscription.getEndedAt()
        );
    }

    @Override
    public void updateDefaultPaymentMethod(String userId, String paymentMethodId)
            throws PaymentValidationException, StripeException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new PaymentValidationException("UserId é obrigatório");
        }
        if (paymentMethodId == null || paymentMethodId.trim().isEmpty()) {
            throw new PaymentValidationException("PaymentMethodId é obrigatório");
        }

        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        if (!userId.equals(paymentMethod.getCustomer())) {
            throw new PaymentValidationException("Payment method does not belong to this user");
        }

        SubscriptionListParams params = SubscriptionListParams.builder()
                .setCustomer(userId)
                .setStatus(SubscriptionListParams.Status.ACTIVE)
                .build();

        SubscriptionCollection subscriptions = Subscription.list(params);
        for (Subscription subscription : subscriptions.getData()) {
            SubscriptionUpdateParams updateParams = SubscriptionUpdateParams.builder()
                    .setDefaultPaymentMethod(paymentMethodId)
                    .build();
            subscription.update(updateParams);
        }

        CustomerUpdateParams customerParams = CustomerUpdateParams.builder()
                .setInvoiceSettings(
                        CustomerUpdateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(paymentMethodId)
                                .build()
                )
                .build();

        Customer.retrieve(userId).update(customerParams);

        log.info("Successfully updated default payment method to {} for user {}",
                paymentMethodId, userId);
    }

    @Override
    public List<TransactionDTO> getCustomerTransactions(String customerId) throws StripeException {

        List<TransactionDTO> transactions = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("limit", 100);

        ChargeCollection charges = Charge.list(params);

        for (Charge charge : charges.getData()) {
            TransactionDTO transaction = new TransactionDTO();

            Instant createdAt = Instant.ofEpochSecond(charge.getCreated());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                    .withLocale(new Locale("en"));
            String formattedDate = createdAt.atZone(ZoneId.systemDefault())
                    .format(formatter);

            NumberFormat brazilianFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String formattedAmount = brazilianFormat.format(
                    BigDecimal.valueOf(charge.getAmount()).divide(BigDecimal.valueOf(100)));

            transaction.setData(formattedDate);
            transaction.setNumeroDoRecibo(charge.getId());
            transaction.setValorTotal(formattedAmount);
            transaction.setReceiptUrl(charge.getReceiptUrl());

            transactions.add(transaction);
        }

        return transactions;
    }
}

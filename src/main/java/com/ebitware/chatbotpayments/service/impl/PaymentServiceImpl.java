package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.entity.FormSubmission;
import com.ebitware.chatbotpayments.exception.PaymentValidationException;
import com.ebitware.chatbotpayments.model.PaymentSuccessEvent;
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
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final PaymentSuccessEmailService emailService;

    public PaymentServiceImpl(
            @Value("${stripe.secret-key}") String stripeSecretKey,
            @Value("${payment.currency}") String currency,
            @Value("${payment.min-amount}") long minAmount,
            @Value("${payment.max-amount}") long maxAmount,
            BrlCustomerRepository brlCustomerRepository,
            BrlPaymentRepository brlPaymentRepository,
            BrlSubscriptionRepository brlSubscriptionRepository, FormSubmissionRepository formSubmissionRepository, PaymentSuccessEmailService emailService
    ) {
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
            BrlCustomer customer = existingCustomer.get();
            log.info("Found existing customer with document {} and type {}",
                    document, documentType);

            // Create or update metadata with new person_id
            Map<String, String> updatedMetadata = new HashMap<>(customer.getMetadata());
            updatedMetadata.put("person_id", personId.toString());

            // Always update the record with the new person_id and metadata
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
            FormSubmission form = formSubmissionRepository.findByPersonId(
                            Integer.parseInt(payload.get("personId").toString()))
                    .orElseThrow(() -> new PaymentValidationException("Form submission not found"));

            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .companyName(form.getBusinessName())
                    .bmId(form.getFacebookManagerNo())
                    .commercialName(form.getDisplayName())
                    .phone(form.getPhone())
                    .email(form.getCorporateEmail())
                    .website(form.getWebsite())
                    .address(form.getAddress())
                    .vertical(form.getVertical())
                    .businessDescription(form.getDescription())
                    .planName(String.valueOf(payload.getOrDefault("planName", "Basic")))
                    .planValue(formatAmount(amount))
                    .currency(currency.toUpperCase())
                    .contractPeriod(String.valueOf(payload.getOrDefault("contractPeriod", "Monthly")))
                    .startDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .endDate(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .agents(String.valueOf(payload.getOrDefault("agents", "1")))
                    .addons(String.valueOf(payload.getOrDefault("addons", "Basic")))
                    .monthlyVolume(String.valueOf(payload.getOrDefault("monthlyVolume", "1000")))
                    .channels(String.valueOf(payload.getOrDefault("channels", "WhatsApp")))
                    .paymentDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setupFee(formatAmount(amount))
                    .customerEmail(form.getCorporateEmail())
                    .build();

            emailService.sendPaymentSuccessEmails(event);
            log.info("Success emails sent for payment from customer: {}", customerId);
        } catch (Exception e) {
            log.error("Error sending success emails: {}", e.getMessage());
        }
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

                        // Format date
                        LocalDateTime dateTime = ((Timestamp) payment.get("created_at")).toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                        receipt.put("date", dateTime.format(formatter));

                        // Format amount
                        BigDecimal amount = (BigDecimal) payment.get("amount");
                        String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("en", "US"))
                                .format(amount);
                        receipt.put("totalAmount", formattedAmount);

                        // Payment ID (Stripe payment intent ID)
                        String paymentId = (String) payment.get("id");
                        receipt.put("paymentId", paymentId);

                        // Receipt number (last 8 chars of payment ID)
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
}

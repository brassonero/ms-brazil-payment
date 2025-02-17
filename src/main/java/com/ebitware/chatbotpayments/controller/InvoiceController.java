package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.entity.BrlInvoice;
import com.ebitware.chatbotpayments.model.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceItem;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostMapping("/create")
    public ResponseEntity<StripeInvoiceResponse> createInvoice(@RequestBody StripeInvoiceRequest request) {
        try {
            Stripe.apiKey = stripeSecretKey;

            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setEmail(request.getCustomerEmail())
                    .setName(request.getCustomerName())
                    .build();
            Customer customer = Customer.create(customerParams);

            InvoiceItemCreateParams invoiceItemParams = InvoiceItemCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setAmount(request.getAmountInCents())
                    .setCurrency("brl")
                    .setDescription(request.getDescription())
                    .build();
            InvoiceItem.create(invoiceItemParams);

            InvoiceCreateParams invoiceParams = InvoiceCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setAutoAdvance(true)
                    .build();
            Invoice invoice = Invoice.create(invoiceParams);

            Invoice finalizedInvoice = invoice.finalizeInvoice();

            String pdfUrl = finalizedInvoice.getInvoicePdf();

            return ResponseEntity.ok(new StripeInvoiceResponse(finalizedInvoice.getId(), pdfUrl));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StripeInvoiceResponse(null, "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/billing")
    public ResponseEntity<String> createBillingInfo(@RequestBody InvoceRequest request) {
        boolean created = service.createBillingInfo(request);
        return created
                ? ResponseEntity.ok("Billing information created successfully.")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create billing information.");
    }

    @PutMapping("/billing/{email}")
    public ResponseEntity<String> updateBillingInfo(@PathVariable String email, @RequestBody InvoceRequest request) {
        boolean updated = service.updateBillingInfo(email, request);
        return updated
                ? ResponseEntity.ok("Billing information updated successfully.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Billing information not found.");
    }

    @GetMapping("/info/{email}")
    public ResponseEntity<BillingInfoResponse> getBillingInfo(@PathVariable String email) {
        try {
            BrlInvoice invoice = service.getBillingInfoByEmail(email);
            if (invoice == null) {
                return ResponseEntity.notFound().build();
            }

            BillingInfoResponse response = BillingInfoResponse.builder()
                    .rfc(invoice.getTaxId())
                    .nombreRazonSocial(invoice.getBusinessName())
                    .regimeTributario(invoice.getFiscalRegime())
                    .usoCFDI(invoice.getCfdiUsage())
                    .emailCobranca(invoice.getBillingEmail())
                    .facturamentoAutomatico(true)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving billing information for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

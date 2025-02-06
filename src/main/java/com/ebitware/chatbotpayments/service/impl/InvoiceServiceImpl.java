package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.entity.BrlInvoice;
import com.ebitware.chatbotpayments.model.InvoceRequest;
import com.ebitware.chatbotpayments.repository.billing.InvoiceRepository;
import com.ebitware.chatbotpayments.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;

    @Override
    @Transactional
    public boolean createBillingInfo(InvoceRequest request) {
        try {
            log.debug("Creating billing info for request: {}", request);
            BrlInvoice invoice = mapToBillingInfo(request);
            int generatedId = repository.save(invoice);

            boolean success = generatedId > 0;
            if (success) {
                log.debug("Successfully created billing info with ID: {}", generatedId);
            } else {
                log.error("Failed to create billing info");
            }
            return success;
        } catch (Exception e) {
            log.error("Error creating billing info: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean updateBillingInfo(String email, InvoceRequest request) {
        BrlInvoice brlInvoiceDTO = mapToBillingInfo(request);
        return repository.update(email, brlInvoiceDTO) > 0;
    }

    @Override
    public BrlInvoice getBillingInfoByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Billing information not found for email: " + email));
    }

    @Override
    public BrlInvoice mapToBillingInfo(InvoceRequest request) {
        BrlInvoice invoice = new BrlInvoice();
        invoice.setFiscalRegime(request.getFiscalRegime());
        invoice.setBusinessName(request.getBusinessName());
        invoice.setIdType(request.getIdType());
        invoice.setIdNumber(request.getIdNumber());
        invoice.setBillingEmail(request.getBillingEmail());
        invoice.setPhone(request.getPhone());

        if (request.getAddress() != null) {
            invoice.setStreet(request.getAddress().getStreet());
            invoice.setNeighborhood(request.getAddress().getNeighborhood());
            invoice.setPostalCode(request.getAddress().getPostalCode());
            invoice.setCountry(request.getAddress().getCountry());
            invoice.setState(request.getAddress().getState());
            invoice.setCity(request.getAddress().getCity());
        }

        invoice.setTaxId(request.getTaxId());
        invoice.setCfdiUsage(request.getCfdiUsage());
        invoice.setPersonId(request.getPersonId());

        log.debug("Mapped request to invoice: {}", invoice);
        return invoice;
    }
}

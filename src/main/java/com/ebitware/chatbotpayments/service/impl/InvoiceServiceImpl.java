package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.InvoiceEntity;
import com.ebitware.chatbotpayments.model.InvoceRequest;
import com.ebitware.chatbotpayments.repository.billing.InvoiceRepository;
import com.ebitware.chatbotpayments.service.InvoiceService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;

    @Override
    public boolean createBillingInfo(InvoceRequest request) {
        InvoiceEntity invoiceEntityDTO = mapToBillingInfo(request);
        return repository.save(invoiceEntityDTO) > 0;
    }

    @Override
    public boolean updateBillingInfo(String email, InvoceRequest request) {
        InvoiceEntity invoiceEntityDTO = mapToBillingInfo(request);
        return repository.update(email, invoiceEntityDTO) > 0;
    }

    @Override
    public InvoiceEntity getBillingInfoByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Billing information not found for email: " + email));
    }

    @Override
    public InvoiceEntity mapToBillingInfo(InvoceRequest request) {
        InvoiceEntity invoiceEntityDTO = new InvoiceEntity();
        invoiceEntityDTO.setFiscalRegime(request.getFiscalRegime());
        invoiceEntityDTO.setBusinessName(request.getBusinessName());
        invoiceEntityDTO.setIdType(request.getIdType());
        invoiceEntityDTO.setIdNumber(request.getIdNumber());
        invoiceEntityDTO.setBillingEmail(request.getBillingEmail());
        invoiceEntityDTO.setPhone(request.getPhone());
        invoiceEntityDTO.setStreet(request.getAddress() != null ? request.getAddress().getStreet() : null);
        invoiceEntityDTO.setNeighborhood(request.getAddress() != null ? request.getAddress().getNeighborhood() : null);
        invoiceEntityDTO.setPostalCode(request.getAddress() != null ? request.getAddress().getPostalCode() : null);
        invoiceEntityDTO.setCountry(request.getAddress() != null ? request.getAddress().getCountry() : null);
        invoiceEntityDTO.setState(request.getAddress() != null ? request.getAddress().getState() : null);
        invoiceEntityDTO.setCity(request.getAddress() != null ? request.getAddress().getCity() : null);
        invoiceEntityDTO.setTaxId(request.getTaxId());
        invoiceEntityDTO.setCfdiUsage(request.getCfdiUsage());
        return invoiceEntityDTO;
    }
}

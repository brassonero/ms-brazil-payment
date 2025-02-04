package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.InvoiceEntity;
import com.ebitware.chatbotpayments.model.InvoceRequest;
import com.ebitware.chatbotpayments.repository.billing.LastInvoiceRepository;
import com.ebitware.chatbotpayments.service.LastInvoiceService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LastInvoiceServiceImpl implements LastInvoiceService {

    private final LastInvoiceRepository repository;

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
    public InvoiceEntity mapToBillingInfo(InvoceRequest request) {
        InvoiceEntity invoiceEntityDTO = new InvoiceEntity();
        invoiceEntityDTO.setFiscalRegime(request.getFiscalRegime());
        invoiceEntityDTO.setBusinessName(request.getBusinessName());
        invoiceEntityDTO.setIdType(request.getIdType());
        invoiceEntityDTO.setIdNumber(request.getIdNumber());
        invoiceEntityDTO.setBillingEmail(request.getBillingEmail());
        invoiceEntityDTO.setPhone(request.getPhone());
        invoiceEntityDTO.setStreet(request.getAddress() != null ? request.getAddress().getStreet() : request.getStreet());
        invoiceEntityDTO.setNeighborhood(request.getAddress() != null ? request.getAddress().getNeighborhood() : null);
        invoiceEntityDTO.setPostalCode(request.getAddress() != null ? request.getAddress().getPostalCode() : request.getPostalCode());
        invoiceEntityDTO.setCountry(request.getAddress() != null ? request.getAddress().getCountry() : request.getCountry());
        invoiceEntityDTO.setState(request.getAddress() != null ? request.getAddress().getState() : request.getState());
        invoiceEntityDTO.setCity(request.getAddress() != null ? request.getAddress().getCity() : request.getCity());
        invoiceEntityDTO.setTaxId(request.getTaxId());
        invoiceEntityDTO.setCfdiUsage(request.getCfdiUsage());
        return invoiceEntityDTO;
    }
}

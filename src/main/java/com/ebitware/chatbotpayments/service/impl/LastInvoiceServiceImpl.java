package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.InvoiceDTO;
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
        InvoiceDTO invoiceDTO = mapToBillingInfo(request);
        return repository.save(invoiceDTO) > 0;
    }

    @Override
    public boolean updateBillingInfo(String email, InvoceRequest request) {
        InvoiceDTO invoiceDTO = mapToBillingInfo(request);
        return repository.update(email, invoiceDTO) > 0;
    }

    @Override
    public InvoiceDTO mapToBillingInfo(InvoceRequest request) {
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setFiscalRegime(request.getFiscalRegime());
        invoiceDTO.setBusinessName(request.getBusinessName());
        invoiceDTO.setIdType(request.getIdType());
        invoiceDTO.setIdNumber(request.getIdNumber());
        invoiceDTO.setBillingEmail(request.getBillingEmail());
        invoiceDTO.setPhone(request.getPhone());
        invoiceDTO.setStreet(request.getAddressDTO() != null ? request.getAddressDTO().getStreet() : request.getStreet());
        invoiceDTO.setNeighborhood(request.getAddressDTO() != null ? request.getAddressDTO().getNeighborhood() : null);
        invoiceDTO.setPostalCode(request.getAddressDTO() != null ? request.getAddressDTO().getPostalCode() : request.getPostalCode());
        invoiceDTO.setCountry(request.getAddressDTO() != null ? request.getAddressDTO().getCountry() : request.getCountry());
        invoiceDTO.setState(request.getAddressDTO() != null ? request.getAddressDTO().getState() : request.getState());
        invoiceDTO.setCity(request.getAddressDTO() != null ? request.getAddressDTO().getCity() : request.getCity());
        invoiceDTO.setTaxId(request.getTaxId());
        invoiceDTO.setCfdiUsage(request.getCfdiUsage());
        return invoiceDTO;
    }
}

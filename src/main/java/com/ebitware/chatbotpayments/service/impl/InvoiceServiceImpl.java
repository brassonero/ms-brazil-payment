package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.entity.BrlInvoice;
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
        BrlInvoice brlInvoiceDTO = mapToBillingInfo(request);
        return repository.save(brlInvoiceDTO) > 0;
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
        BrlInvoice brlInvoiceDTO = new BrlInvoice();
        brlInvoiceDTO.setFiscalRegime(request.getFiscalRegime());
        brlInvoiceDTO.setBusinessName(request.getBusinessName());
        brlInvoiceDTO.setIdType(request.getIdType());
        brlInvoiceDTO.setIdNumber(request.getIdNumber());
        brlInvoiceDTO.setBillingEmail(request.getBillingEmail());
        brlInvoiceDTO.setPhone(request.getPhone());
        brlInvoiceDTO.setStreet(request.getAddress() != null ? request.getAddress().getStreet() : null);
        brlInvoiceDTO.setNeighborhood(request.getAddress() != null ? request.getAddress().getNeighborhood() : null);
        brlInvoiceDTO.setPostalCode(request.getAddress() != null ? request.getAddress().getPostalCode() : null);
        brlInvoiceDTO.setCountry(request.getAddress() != null ? request.getAddress().getCountry() : null);
        brlInvoiceDTO.setState(request.getAddress() != null ? request.getAddress().getState() : null);
        brlInvoiceDTO.setCity(request.getAddress() != null ? request.getAddress().getCity() : null);
        brlInvoiceDTO.setTaxId(request.getTaxId());
        brlInvoiceDTO.setCfdiUsage(request.getCfdiUsage());
        return brlInvoiceDTO;
    }
}

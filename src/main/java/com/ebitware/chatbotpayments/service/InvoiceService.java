package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.InvoiceDTO;
import com.ebitware.chatbotpayments.model.InvoceRequest;

public interface InvoiceService {
    boolean createBillingInfo(InvoceRequest request);
    boolean updateBillingInfo(String email, InvoceRequest request);
    InvoiceDTO mapToBillingInfo(InvoceRequest request);
}

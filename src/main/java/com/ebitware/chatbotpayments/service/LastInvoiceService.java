package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.model.InvoiceEntity;
import com.ebitware.chatbotpayments.model.InvoceRequest;

public interface LastInvoiceService {
    boolean createBillingInfo(InvoceRequest request);
    boolean updateBillingInfo(String email, InvoceRequest request);
    InvoiceEntity mapToBillingInfo(InvoceRequest request);
}

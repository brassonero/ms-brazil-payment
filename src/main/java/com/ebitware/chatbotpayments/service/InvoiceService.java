package com.ebitware.chatbotpayments.service;

import com.ebitware.chatbotpayments.entity.BrlInvoice;
import com.ebitware.chatbotpayments.model.InvoceRequest;

public interface InvoiceService {
    boolean createBillingInfo(InvoceRequest request);
    boolean updateBillingInfo(String email, InvoceRequest request);
    BrlInvoice mapToBillingInfo(InvoceRequest request);
    BrlInvoice getBillingInfoByEmail(String email);
}

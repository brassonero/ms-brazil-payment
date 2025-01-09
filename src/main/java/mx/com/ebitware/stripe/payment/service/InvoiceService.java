package mx.com.ebitware.stripe.payment.service;

import mx.com.ebitware.stripe.payment.model.InvoiceDTO;
import mx.com.ebitware.stripe.payment.model.InvoceRequest;

public interface InvoiceService {
    boolean createBillingInfo(InvoceRequest request);
    boolean updateBillingInfo(String email, InvoceRequest request);
    InvoiceDTO mapToBillingInfo(InvoceRequest request);
}

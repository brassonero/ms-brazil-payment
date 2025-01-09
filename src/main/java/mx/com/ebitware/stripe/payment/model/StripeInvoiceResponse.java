package mx.com.ebitware.stripe.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeInvoiceResponse {
    private String invoiceId;
    private String pdfUrl;
}

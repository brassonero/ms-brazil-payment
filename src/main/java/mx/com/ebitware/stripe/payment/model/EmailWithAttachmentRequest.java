package mx.com.ebitware.stripe.payment.model;

import lombok.Data;

@Data
public class EmailWithAttachmentRequest {
    private String to;
    private String subject;
    private String body;
    private String attachmentPath;
}

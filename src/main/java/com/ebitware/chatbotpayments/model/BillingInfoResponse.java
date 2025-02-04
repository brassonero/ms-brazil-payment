package com.ebitware.chatbotpayments.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingInfoResponse {
    private String rfc;
    private String nombreRazonSocial;
    private String regimeTributario;
    private String usoCFDI;
    private String emailCobranca;
    private boolean facturamentoAutomatico;
}

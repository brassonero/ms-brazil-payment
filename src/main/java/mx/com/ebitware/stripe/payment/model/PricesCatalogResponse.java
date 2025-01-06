package mx.com.ebitware.stripe.payment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PricesCatalogResponse {
    private String templateType;
    private double cost;
    private boolean free;
}

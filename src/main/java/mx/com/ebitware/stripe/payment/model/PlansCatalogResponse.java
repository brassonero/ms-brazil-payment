package mx.com.ebitware.stripe.payment.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlansCatalogResponse {
    private List<PlansDTO> plans;
    private List<PricesDTO> waPrices;
}

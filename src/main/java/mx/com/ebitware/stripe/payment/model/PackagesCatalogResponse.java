package mx.com.ebitware.stripe.payment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PackagesCatalogResponse {
    private String packageName;
    private int conversations;
    private double cost;
}

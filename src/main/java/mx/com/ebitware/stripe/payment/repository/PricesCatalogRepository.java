package mx.com.ebitware.stripe.payment.repository;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PricesCatalogResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static mx.com.ebitware.stripe.payment.constants.SqlConstants.SELECT_ALL_WA_PRICES;

@Repository
@RequiredArgsConstructor
public class PricesCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<PricesCatalogResponse> pricingRowMapper = (rs, rowNum) ->
            PricesCatalogResponse.builder()
                    .templateType(rs.getString("template_type"))
                    .cost(rs.getDouble("cost"))
                    .free(rs.getBoolean("is_free"))
                    .build();

    public List<PricesCatalogResponse> findAllPricing() {
        return jdbcTemplate.query(SELECT_ALL_WA_PRICES, pricingRowMapper);
    }
}

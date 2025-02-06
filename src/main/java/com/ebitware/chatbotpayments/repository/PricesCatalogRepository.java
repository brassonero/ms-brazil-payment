package com.ebitware.chatbotpayments.repository;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.PricesDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ebitware.chatbotpayments.constants.SqlConstants.SELECT_ALL_WA_PRICES;

@Repository
@RequiredArgsConstructor
public class PricesCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<PricesDTO> pricingRowMapper = (rs, rowNum) ->
            PricesDTO.builder()
                    .templateType(rs.getString("template_type"))
                    .cost(rs.getDouble("cost"))
                    .free(rs.getBoolean("is_free"))
                    .build();

    public List<PricesDTO> findAllPricing() {
        return jdbcTemplate.query(SELECT_ALL_WA_PRICES, pricingRowMapper);
    }
}

package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.PricesDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.ebitware.chatbotpayments.constants.SqlConstants.SELECT_ALL_WA_PRICES;

@Repository
public class PricesCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PricesCatalogRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

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

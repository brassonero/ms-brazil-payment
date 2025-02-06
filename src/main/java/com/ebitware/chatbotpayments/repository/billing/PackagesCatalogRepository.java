package com.ebitware.chatbotpayments.repository.billing;

import com.ebitware.chatbotpayments.model.PackagesCatalogResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.ebitware.chatbotpayments.constants.SqlConstants.SELECT_ALL_PACKAGES;

@Repository
public class PackagesCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PackagesCatalogRepository(@Qualifier("billingDataSource") DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    private final RowMapper<PackagesCatalogResponse> packageMapper = (rs, rowNum) ->
            PackagesCatalogResponse.builder()
                    .packageName(rs.getString("package_name"))
                    .conversations(rs.getInt("conversations"))
                    .cost(rs.getDouble("cost"))
                    .build();

    public List<PackagesCatalogResponse> findAllPackages() {
        return jdbcTemplate.query(SELECT_ALL_PACKAGES, packageMapper);
    }
}

package com.ebitware.chatbotpayments.repository;

import lombok.RequiredArgsConstructor;
import com.ebitware.chatbotpayments.model.PackagesCatalogResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ebitware.chatbotpayments.constants.SqlConstants.SELECT_ALL_PACKAGES;

@Repository
@RequiredArgsConstructor
public class PackagesCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

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

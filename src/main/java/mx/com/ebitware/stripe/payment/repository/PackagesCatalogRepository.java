package mx.com.ebitware.stripe.payment.repository;

import lombok.RequiredArgsConstructor;
import mx.com.ebitware.stripe.payment.model.PackagesCatalogResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static mx.com.ebitware.stripe.payment.constants.SqlConstants.SELECT_ALL_PACKAGES;

@Repository
@RequiredArgsConstructor
public class PackagesCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<PackagesCatalogResponse> packageMapper = (rs, rowNum) ->
            PackagesCatalogResponse.builder()
                    .conversations(rs.getInt("conversations"))
                    .cost(rs.getDouble("cost"))
                    .build();

    public List<PackagesCatalogResponse> findAllPackages() {
        return jdbcTemplate.query(SELECT_ALL_PACKAGES, packageMapper);
    }
}

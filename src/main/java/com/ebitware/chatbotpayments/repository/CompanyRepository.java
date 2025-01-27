package com.ebitware.chatbotpayments.repository;

import com.ebitware.chatbotpayments.model.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.ebitware.chatbotpayments.constants.SqlConstants.*;

@Repository
public class CompanyRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public CompanyRepository(NamedParameterJdbcTemplate jdbcTemplate, BCryptPasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("email", email);
        return jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class) > 0;
    }

    public boolean existsByCompanyName(String name) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", name);
        return jdbcTemplate.queryForObject(CHECK_COMPANY_EXISTS, params, Integer.class) > 0;
    }

    public List<String> findUsernamesLike(String baseUsername) {

        return jdbcTemplate.queryForList(FIND_USERNAMES_LIKE,
                new MapSqlParameterSource("baseUsername", baseUsername),
                String.class);
    }

    @Transactional
    public Long createCompany(WorkspaceDTO request, String password, String username) {
        Long companyId = createCompanyBase(request);
        createUser(companyId, request.getUser(), password, username);

        if (!CompanyModeEnum.API.getCode().equals(request.getMode())) {
            createCompanyAccess(companyId, request.getAccessList());
            validateAndCreateWorkgroup(companyId);
        }

        if (!WorkgroupEnum.PREDETERMINED.getValue().equals(request.getTypeWorkgroups())) {
            createBots(request.getBots(), companyId, request.getTypeWorkgroups());
        }

        return companyId;
    }

    private Long createCompanyBase(WorkspaceDTO request) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", request.getName())
                .addValue("keyName", request.getKeyName())
                .addValue("mode", request.getMode())
                .addValue("agents", request.getAgents())
                .addValue("multipleAgents", request.isMultipleAgents())
                .addValue("supervisors", request.getSupervisors())
                .addValue("multipleSupervisors", request.isMultipleSupervisors())
                .addValue("contacts", request.getContacts())
                .addValue("typeWorkgroups", request.getTypeWorkgroups())
                .addValue("workgroups", request.getWorkgroups())
                .addValue("workgroupsActive", request.isWorkgroupsActive())
                .addValue("active", request.isActive());

        return jdbcTemplate.queryForObject(INSERT_COMPANY, params, Long.class);
    }

    private void createUser(Long companyId, UserDTO user, String password, String username) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("secondLastName", user.getSecondLastName())
                .addValue("username", username)
                .addValue("password", passwordEncoder.encode(password))
                .addValue("email", user.getEmail())
                .addValue("companyId", companyId);

        jdbcTemplate.update(INSERT_PERSON, params);
    }

    private void createCompanyAccess(Long companyId, List<AccessDTO> accessList) {
        for (AccessDTO access : accessList) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("companyId", companyId)
                    .addValue("accessId", access.getId());
            jdbcTemplate.update(INSERT_COMPANY_ACCESS, params);
        }
    }

    private void validateAndCreateWorkgroup(Long companyId) {
        MapSqlParameterSource countParams = new MapSqlParameterSource()
                .addValue("companyId", companyId);

        Integer workgroupCount = jdbcTemplate.queryForObject(
                COUNT_WORKGROUPS,
                countParams,
                Integer.class
        );

        Map<String, Object> companyDetails = jdbcTemplate.queryForMap(
                GET_COMPANY_DETAILS,
                countParams
        );

        boolean isDefaultWorkgroup = workgroupCount == 0;
        String typeWorkgroups = (String) companyDetails.get("type_workgroups");
        Integer maxWorkgroups = (Integer) companyDetails.get("workgroups");

        if (!WorkgroupEnum.PREDETERMINED.getValue().equals(typeWorkgroups)
                && workgroupCount >= maxWorkgroups) {
            throw new RuntimeException("Workgroup limit exceeded");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("fromSystem", isDefaultWorkgroup)
                .addValue("persons", new Integer[]{})
                .addValue("isDefault", isDefaultWorkgroup);

        jdbcTemplate.update(INSERT_WORKGROUP, params);
    }

    private void createBots(List<BotDTO> bots, Long companyId, String typeWorkgroups) {
        boolean isIvr = WorkgroupEnum.IVR.getValue().equals(typeWorkgroups);

        for (BotDTO bot : bots) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", isIvr ? WorkgroupEnum.IVR.getValue() : bot.getName())
                    .addValue("host", "http://10.254.40.12")
                    .addValue("port", isIvr ? WorkgroupEnum.IVR.name() : bot.getPort())
                    .addValue("companyId", companyId)
                    .addValue("ivr", isIvr)
                    .addValue("message", bot.getMessage())
                    .addValue("status", "1")
                    .addValue("botChannel",
                            "apple_channel".equals(bot.getBotChannel()) ? bot.getBotChannel() : null);

            jdbcTemplate.update(INSERT_BOT, params);
        }
    }
}

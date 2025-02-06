package com.ebitware.chatbotpayments.repository;

import com.ebitware.chatbotpayments.model.*;
import com.ebitware.chatbotpayments.service.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PersonRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM chatbot.person WHERE LOWER(email) = LOWER(:email) AND deleted_at IS NULL";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("email", email);
        return jdbcTemplate.queryForObject(sql, params, Integer.class) > 0;
    }

    @Transactional
    public Long createCompany(WorkspaceDTO request, String password) {
        Long companyId = createCompanyBase(request);
        createUser(companyId, request.getUser(), password);

        if (!CompanyModeEnum.API.getCode().equals(request.getMode())) {
            validateAndCreateWorkgroup(companyId);
        }

        if (!WorkgroupEnum.PREDETERMINED.getValue().equals(request.getTypeWorkgroups())) {
            createBots(request.getBots(), companyId, request.getTypeWorkgroups());
        }

        return companyId;
    }

    private Long createCompanyBase(WorkspaceDTO request) {
        String sql = """
                INSERT INTO chatbot.company (
                    name, key_name, mode, agents, multiple_agents, supervisors,
                    multiple_supervisors, contacts, type_workgroups, workgroups,
                    workgroups_active, active, created_at, updated_at
                ) VALUES (
                    :name, :keyName, :mode, CAST(:agents AS INTEGER), :multipleAgents, CAST(:supervisors AS INTEGER),
                    :multipleSupervisors, CAST(:contacts AS INTEGER), :typeWorkgroups, CAST(:workgroups AS INTEGER),
                    :workgroupsActive, :active, NOW(), NOW()
                ) RETURNING id
                """;

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

        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    private void validateAndCreateWorkgroup(Long companyId) {
        String countSql = """
                SELECT COUNT(*) FROM chatbot.workgroup 
                WHERE company_id = :companyId AND deleted_at IS NULL
                """;
        MapSqlParameterSource countParams = new MapSqlParameterSource()
                .addValue("companyId", companyId);
        Integer workgroupCount = jdbcTemplate.queryForObject(countSql, countParams, Integer.class);

        String companySql = """
                SELECT type_workgroups, workgroups 
                FROM chatbot.company 
                WHERE id = :companyId
                """;
        MapSqlParameterSource companyParams = new MapSqlParameterSource()
                .addValue("companyId", companyId);
        Map<String, Object> companyDetails = jdbcTemplate.queryForMap(companySql, companyParams);

        boolean isDefaultWorkgroup = workgroupCount == 0;
        String typeWorkgroups = (String) companyDetails.get("type_workgroups");
        Integer maxWorkgroups = (Integer) companyDetails.get("workgroups");

        if (!WorkgroupEnum.PREDETERMINED.getValue().equals(typeWorkgroups)
                && workgroupCount >= maxWorkgroups) {
            throw new RuntimeException("Workgroup limit exceeded");
        }

        String workgroupSql = """
                INSERT INTO chatbot.workgroup (
                    name, description, company_id, active,
                    from_system, created_at, updated_at,
                    persons, is_default
                ) VALUES (
                    'General', 'Default', :companyId, true,
                    :fromSystem, NOW(), NOW(),
                    :persons, :isDefault
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", companyId)
                .addValue("fromSystem", isDefaultWorkgroup)
                .addValue("persons", new Integer[]{})
                .addValue("isDefault", isDefaultWorkgroup);

        jdbcTemplate.update(workgroupSql, params);
    }

    private void createBots(List<BotDTO> bots, Long companyId, String typeWorkgroups) {
        String sql = """
                INSERT INTO chatbot.bot_host (
                    name, host, port, company_id, ivr,
                    message, status, active, bot_channel,
                    created_at, updated_at
                ) VALUES (
                    :name, :host, :port, :companyId, :ivr,
                    :message, :status, true, :botChannel,
                    NOW(), NOW()
                )
                """;

        boolean isIvr = WorkgroupEnum.IVR.getValue().equals(typeWorkgroups);

        for (BotDTO bot : bots) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", isIvr ? WorkgroupEnum.IVR.getValue() : bot.getName())
                    .addValue("host", "http://10.254.40.12")
                    .addValue("port", isIvr ? WorkgroupEnum.IVR.getValue() : bot.getPort())
                    .addValue("companyId", companyId)
                    .addValue("ivr", isIvr)
                    .addValue("message", bot.getMessage())
                    .addValue("status", "1")
                    .addValue("botChannel",
                            "apple_channel".equals(bot.getBotChannel()) ? bot.getBotChannel() : null);

            jdbcTemplate.update(sql, params);
        }
    }

    private void createUser(Long companyId, UserDTO user, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        String username = user.getEmail().split("@")[0];

        String sql = """
                INSERT INTO chatbot.person (
                    first_name, last_name, second_last_name,
                    username, password, email, active, first_login,
                    role_id, company_id, is_super,
                    created_at, updated_at
                ) VALUES (
                    :firstName, :lastName, :secondLastName,
                    :username, :password, :email, true, true,
                    1, :companyId, true,
                    NOW(), NOW()
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("secondLastName", user.getSecondLastName())
                .addValue("username", username)
                .addValue("password", hashedPassword)
                .addValue("email", user.getEmail())
                .addValue("companyId", companyId);

        jdbcTemplate.update(sql, params);
    }
}

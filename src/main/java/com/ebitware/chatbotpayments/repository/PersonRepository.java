package com.ebitware.chatbotpayments.repository;

import com.ebitware.chatbotpayments.entity.Person;
import com.ebitware.chatbotpayments.model.WorkspaceDTO;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PersonRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PersonRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM chatbot.person WHERE LOWER(email) = LOWER(:email) AND deleted_at IS NULL";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("email", email);
        return jdbcTemplate.queryForObject(sql, params, Integer.class) > 0;
    }


    public Long createCompany(WorkspaceDTO request) {
        String sql = """
            INSERT INTO chatbot.company (
                name, key_name, mode, agents, multiple_agents, supervisors,
                multiple_supervisors, contacts, type_workgroups, workgroups,
                workgroups_active, active, created_at, updated_at
            ) VALUES (
                :name, :keyName, :mode, CAST(:agents AS INTEGER), :multipleAgents, CAST(:supervisors AS INTEGER),
                :multipleSupervisors, CAST(:contacts AS INTEGER), :typeWorkgroups, CAST(:workgroups AS INTEGER),
                :workgroupsActive, :active, NOW(), NOW()
            )
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

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }
}

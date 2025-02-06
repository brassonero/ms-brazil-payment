package com.ebitware.chatbotpayments.util;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsernameGeneratorUtil {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public String generateUsername(String firstName, String lastName, String secondLastName) {
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            throw new RuntimeException("First name and last name are required");
        }

        String baseUsername = generateBaseUsername(firstName, lastName,
                secondLastName != null ? secondLastName : "");
        return getUniqueUsername(baseUsername);
    }

    private String generateBaseUsername(String firstName, String lastName, String secondLastName) {
        String username = (firstName.length() > 0 ? firstName.substring(0, 1) : "") +
                lastName +
                (secondLastName.length() > 0 ? secondLastName.substring(0, 1) : "");
        return removeAccentsAndSpecialChars(username.toLowerCase());
    }

    private String getUniqueUsername(String baseUsername) {
        String sql = """
            SELECT username 
            FROM chatbot.person 
            WHERE LOWER(username) LIKE LOWER(:baseUsername) || '%'
            AND deleted_at IS NULL
            ORDER BY username DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("baseUsername", baseUsername);

        List<String> existingUsernames = jdbcTemplate.queryForList(sql, params, String.class);

        if (existingUsernames.isEmpty()) {
            return baseUsername;
        }

        int counter = 1;
        String newUsername = baseUsername;
        while (existingUsernames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(newUsername.toLowerCase())) {
            newUsername = baseUsername + counter++;
        }

        return newUsername;
    }

    private String removeAccentsAndSpecialChars(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[^a-zA-Z0-9]", "");
    }
}

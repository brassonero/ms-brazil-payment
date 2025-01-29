package com.ebitware.chatbotpayments.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ChatbotJdbcConfig {

    @Bean(name = "chatbotDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.chatbot")
    public DataSource chatbotDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://10.254.40.16:5432/chatbot")
                .username("chatbot_app_security")
                .password("h-86E$Epru=6es*uplWR")
                .build();
    }

    @Bean(name = "chatbotJdbcTemplate")
    public NamedParameterJdbcTemplate chatbotJdbcTemplate(
            @Qualifier("chatbotDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

package com.ebitware.chatbotpayments.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class BillingJdbcConfig {

    @Primary
    @Bean(name = "billingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.billing")
    public DataSource billingDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://10.254.40.16:5432/billing")
                .username("chatbot_app_billing")
                .password("dfv0d90932i0csdcjanlewq")
                .build();
    }

    @Primary
    @Bean(name = "billingJdbcTemplate")
    public NamedParameterJdbcTemplate billingJdbcTemplate(
            @Qualifier("billingDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

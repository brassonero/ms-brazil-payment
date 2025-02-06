package com.ebitware.chatbotpayments.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class OpenApiConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenApiConfig.class);

    @Bean
    @ConditionalOnMissingBean(BuildProperties.class)
    BuildProperties buildProperties(){
        log.info("Loaded build properties");
        return new BuildProperties( new Properties());
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI springOpenAPI(BuildProperties buildProperties) {
        return new OpenAPI()
                .info(new Info().title("chatbot-payments")
                        .description("Initial project ")
                        .version(buildProperties.getVersion())
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("E-Bitware")
                        .url("https://e-bitware.com.mx/"));
    }
}

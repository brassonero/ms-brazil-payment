package com.ebitware.chatbotpayments.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "files")
@Getter
@Setter
public class FileStorageProperties {
    private String uploadDir;
}

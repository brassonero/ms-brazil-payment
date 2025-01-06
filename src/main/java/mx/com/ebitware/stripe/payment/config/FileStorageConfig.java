package mx.com.ebitware.stripe.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class FileStorageConfig {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Bean
    public FileSystemResource fileStorageLocation() {
        return new FileSystemResource(uploadDir);
    }
}


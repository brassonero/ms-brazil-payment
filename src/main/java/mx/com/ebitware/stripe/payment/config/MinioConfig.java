package mx.com.ebitware.stripe.payment.config;


import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("X0FrxCVnz0Pl8jMZsHtg", "tWH7ybmwnUNiIYowRZICfQx5IipOjwI4vFve8r18")
                .build();
    }
}

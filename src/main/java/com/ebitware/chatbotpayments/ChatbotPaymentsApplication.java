package com.ebitware.chatbotpayments;

import com.ebitware.chatbotpayments.config.properties.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class ChatbotPaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotPaymentsApplication.class, args);
	}

}

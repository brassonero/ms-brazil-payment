package mx.com.ebitware.stripe.payment;

import mx.com.ebitware.stripe.payment.config.properties.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class StripePayment {

	public static void main(String[] args) {
		SpringApplication.run(StripePayment.class, args);
	}

}

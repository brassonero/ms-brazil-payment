package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.client.StripeClient;
import com.ebitware.chatbotpayments.entity.BrlCustomer;
import com.ebitware.chatbotpayments.exception.CustomerException;
import com.ebitware.chatbotpayments.exception.StripeApiException;
import com.ebitware.chatbotpayments.model.CreateCustomerRequest;
import com.ebitware.chatbotpayments.model.StripeCustomerResponse;
import com.ebitware.chatbotpayments.repository.billing.BrlCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrlCustomerService {
    private final StripeClient stripeClient;
    private final BrlCustomerRepository brlCustomerRepository;

    public BrlCustomer createCustomer(CreateCustomerRequest request) {
        try {
            Map<String, String> customerDetails = new HashMap<>();
            customerDetails.put("email", request.getEmail());
            customerDetails.put("name", request.getName());
            customerDetails.put("source", request.getSource());

            if (request.getMetadata() != null) {
                request.getMetadata().fields().forEachRemaining(entry ->
                        customerDetails.put("metadata[" + entry.getKey() + "]", entry.getValue().asText())
                );
            }

            log.debug("Creating customer in Stripe: {}", customerDetails);
            StripeCustomerResponse stripeResponse = stripeClient.createCustomer(customerDetails);
            log.debug("Received customer response from Stripe: {}", stripeResponse);

            BrlCustomer customer = new BrlCustomer();
            customer.setStripeCustomerId(stripeResponse.getId());
            customer.setEmail(request.getEmail());
            customer.setName(request.getName());
            customer.setDefaultSource(stripeResponse.getDefaultSource());
            customer.setActive(true);
            customer.setMetadata(request.getMetadata());

            return brlCustomerRepository.save(customer);

        } catch (StripeApiException e) {
            log.error("Stripe API error while creating customer: {}", e.getMessage());
            throw new CustomerException("Error creating customer in Stripe: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating customer", e);
            throw new CustomerException("Unexpected error creating customer", e);
        }
    }
}

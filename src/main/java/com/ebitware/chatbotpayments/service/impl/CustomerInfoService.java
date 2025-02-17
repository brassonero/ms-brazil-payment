package com.ebitware.chatbotpayments.service.impl;

import com.ebitware.chatbotpayments.exception.CustomerNotFoundException;
import com.ebitware.chatbotpayments.model.CustomerInfoDTO;
import com.ebitware.chatbotpayments.repository.billing.CustomerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerInfoService {

    private final CustomerInfoRepository customerInfoRepository;

    public CustomerInfoDTO getCustomerInfo(Integer personId) {
        if (personId == null) {
            throw new IllegalArgumentException("Person ID cannot be null");
        }

        log.debug("Checking if person exists with ID: {}", personId);
        if (!customerInfoRepository.existsByPersonId(personId)) {
            throw new CustomerNotFoundException("Person not found with ID: " + personId);
        }

        log.debug("Fetching customer info for person ID: {}", personId);
        return customerInfoRepository.findCustomerInfoByPersonId(personId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found for person ID: " + personId));
    }
}

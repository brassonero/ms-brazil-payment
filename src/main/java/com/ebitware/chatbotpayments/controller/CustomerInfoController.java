package com.ebitware.chatbotpayments.controller;

import com.ebitware.chatbotpayments.exception.CustomerNotFoundException;
import com.ebitware.chatbotpayments.model.CustomerInfoDTO;
import com.ebitware.chatbotpayments.service.impl.CustomerInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerInfoController {

    private final CustomerInfoService customerInfoService;

    @GetMapping("/{personId}/info")
    public ResponseEntity<?> getCustomerInfo(@PathVariable Integer personId) {
        try {
            CustomerInfoDTO info = customerInfoService.getCustomerInfo(personId);
            return ResponseEntity.ok(info);
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching customer info for person ID {}: {}", personId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

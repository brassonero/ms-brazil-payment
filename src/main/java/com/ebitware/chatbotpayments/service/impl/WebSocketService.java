package com.ebitware.chatbotpayments.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String PAYMENT_EVENTS_TOPIC = "/topic/payment-events";

    public void sendPaymentEvent(String event) {
        try {
            log.info("📤 Sending payment event to WebSocket clients");
            messagingTemplate.convertAndSend(PAYMENT_EVENTS_TOPIC, event);
        } catch (Exception e) {
            log.error("❌ Error sending WebSocket message: {}", e.getMessage(), e);
        }
    }
}

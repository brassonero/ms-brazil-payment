package com.ebitware.chatbotpayments.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //@Value("${cors.allowed.origins:*}")
    private String corsAllowedOrigins;

    // TODO: Remove localhost
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.debug("Registering STOMP endpoint with allowed origin: {}", corsAllowedOrigins);
        registry.addEndpoint("/ws")
                //.setAllowedOrigins(corsAllowedOrigins)
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}

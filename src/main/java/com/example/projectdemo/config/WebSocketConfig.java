package com.example.projectdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wss").setAllowedOrigins("http://10.10.55.57:3000","http://172.20.10.3:3000","http://172.30.1.74:3000");

        // 화상 채팅용 WebSocket 엔드포인트 추가
        registry.addEndpoint("/wss-video")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        //실시간 알림용
        registry.addEndpoint("/wss-notification")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

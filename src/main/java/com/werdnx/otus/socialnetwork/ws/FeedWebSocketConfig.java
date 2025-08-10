package com.werdnx.otus.socialnetwork.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class FeedWebSocketConfig implements WebSocketConfigurer {

    private final FeedWebSocketHandler handler;

    public FeedWebSocketConfig(FeedWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WS путь: ws://host:8080/post/feed/posted?token=...
        registry.addHandler(handler, "/post/feed/posted")
                .setAllowedOrigins("*");
    }
}

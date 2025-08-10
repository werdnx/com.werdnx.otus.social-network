package com.werdnx.otus.socialnetwork.ws;

import com.werdnx.otus.socialnetwork.dto.PostCreatedEvent;
import com.werdnx.otus.socialnetwork.secutiry.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.werdnx.otus.socialnetwork.amqp.AmqpConfig.EXCHANGE_FEED_EVENTS;

@Component
public class FeedWebSocketHandler implements WebSocketHandler {

    private final JwtTokenProvider jwt;
    private final ObjectMapper om;
    private final RabbitAdmin admin;
    private final TopicExchange feedExchange;
    private final org.springframework.amqp.core.Queue wsQueue; // <- явно AMQP-очередь
    private final SimpleMessageListenerContainer container;
    private final Map<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public FeedWebSocketHandler(JwtTokenProvider jwt,
                                ObjectMapper om,
                                RabbitAdmin admin,
                                TopicExchange feedExchange,
                                org.springframework.amqp.core.Queue wsInstanceQueue, // <- явно AMQP-очередь
                                ConnectionFactory cf) {
        this.jwt = jwt;
        this.om = om;
        this.admin = admin;
        this.feedExchange = feedExchange;
        this.wsQueue = wsInstanceQueue;

        // объявляем очередь инстанса
        admin.declareQueue(wsQueue);

        // слушаем очередь инстанса и пушим события в активные WS-сессии адресного пользователя
        SimpleMessageListenerContainer c = new SimpleMessageListenerContainer(cf);
        c.setQueues(wsQueue);
        c.setMessageListener(message -> {
            try {
                String json = new String(message.getBody(), StandardCharsets.UTF_8);
                PostCreatedEvent event = om.readValue(json, PostCreatedEvent.class);
                String rk = message.getMessageProperties().getReceivedRoutingKey();
                Long userId = parseUserId(rk);
                if (userId != null) {
                    var sessions = sessionsByUser.getOrDefault(userId, Collections.emptySet());
                    for (WebSocketSession s : sessions) {
                        if (s.isOpen()) {
                            s.sendMessage(new TextMessage(json));
                        }
                    }
                }
            } catch (Exception ignored) { }
        });
        c.start();
        this.container = c;
    }

    private Long parseUserId(String rk) {
        if (rk == null || !rk.startsWith("user.")) return null;
        try {
            return Long.parseLong(rk.substring("user.".length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = authenticate(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing or invalid token"));
            return;
        }
        sessionsByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        Binding binding = BindingBuilder.bind(wsQueue).to(feedExchange).with("user." + userId);
        admin.declareBinding(binding);
        session.getAttributes().put("userId", userId);
    }

    @Override public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) { /* server-push only */ }
    @Override public void handleTransportError(WebSocketSession session, Throwable exception) { /* no-op */ }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        Object uidObj = session.getAttributes().get("userId");
        if (uidObj instanceof Long userId) {
            var set = sessionsByUser.get(userId);
            if (set != null) {
                set.remove(session);
                if (set.isEmpty()) {
                    admin.removeBinding(new Binding(
                            wsQueue.getName(),
                            Binding.DestinationType.QUEUE,
                            EXCHANGE_FEED_EVENTS,
                            "user." + userId,
                            null));
                }
            }
        }
    }

    @Override public boolean supportsPartialMessages() { return false; }

    private Long authenticate(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return null;
            MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            String token = params.getFirst("token");
            if (token == null || token.isBlank()) return null;
            if (!jwt.validateToken(token)) return null;
            String sub = jwt.getUsername(token);
            return Long.parseLong(sub);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.werdnx.otus.socialnetwork.service;

import com.werdnx.otus.socialnetwork.amqp.AmqpConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeedFanoutProducer {
    private final RabbitTemplate rabbit;

    public FeedFanoutProducer(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void enqueueFanout(Long authorId, Long postId) {
        var payload = java.util.Map.of("authorId", authorId, "postId", postId);
        rabbit.convertAndSend(AmqpConfig.EXCHANGE_MATERIALIZE, "fanout", payload);
    }
}

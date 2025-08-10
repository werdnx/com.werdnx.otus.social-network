package com.werdnx.otus.socialnetwork.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class AmqpConfig {

    public static final String EXCHANGE_FEED_EVENTS = "feed.events";
    public static final String QUEUE_MATERIALIZE = "feed.materialize";
    public static final String EXCHANGE_MATERIALIZE = "feed.materialize.exchange";

    @Bean
    public TopicExchange feedEventsExchange() {
        return new TopicExchange(EXCHANGE_FEED_EVENTS, true, false);
    }

    @Bean
    public DirectExchange materializeExchange() {
        return new DirectExchange(EXCHANGE_MATERIALIZE, true, false);
    }

    @Bean
    public org.springframework.amqp.core.Queue materializeQueue() {
        // quorum queue для надёжности
        return QueueBuilder.durable(QUEUE_MATERIALIZE)
                .quorum()
                .build();
    }

    @Bean
    public Binding materializeBinding() {
        return BindingBuilder
                .bind(materializeQueue())
                .to(materializeExchange())
                .with("fanout");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        return rt;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    // Очередь для текущего инстанса WS-сервиса (биндим на user.<id> при подключении)
    @Bean
    public org.springframework.amqp.core.Queue wsInstanceQueue(
            @Value("${app.ws.instance-id:${random.uuid}}") String instanceId) {
        return QueueBuilder.durable("ws-feed-" + instanceId).build();
    }
}

package com.werdnx.otus.socialnetwork.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@EnableRabbit
public class AmqpConfig {

    public static final String EXCHANGE_FEED_EVENTS = "feed.events";
    public static final String QUEUE_MATERIALIZE   = "feed.materialize";
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
        // quorum очередь для надёжности
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

    /** ObjectMapper, знающий про JavaTime (Instant и т.д.) */
    @Bean
    @Primary
    public ObjectMapper amqpObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper om = builder.build(); // Boot зарегистрирует JavaTimeModule
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 вместо epoch millis
        return om;
    }

    /** Конвертер для AMQP, использующий наш ObjectMapper */
    @Bean
    @Primary
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper amqpObjectMapper) {
        return new Jackson2JsonMessageConverter(amqpObjectMapper);
    }

    /** RabbitTemplate, который ГАРАНТИРОВАННО использует наш конвертер */
    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        return rt;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    /** Очередь инстанса WS-сервиса; будем биндить под конкретных пользователей при подключении */
    @Bean
    public org.springframework.amqp.core.Queue wsInstanceQueue(
            @Value("${app.ws.instance-id:${random.uuid}}") String instanceId) {
        return QueueBuilder.durable("ws-feed-" + instanceId).build();
    }
}

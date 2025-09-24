package com.werdnx.otus.socialnetwork.config;

import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10))
                .additionalInterceptors((request, body, execution) -> {
                    // Сквозной x-request-id
                    String rid = MDC.get("x-request-id");
                    if (rid != null && !rid.isBlank()) {
                        request.getHeaders().set("x-request-id", rid);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}

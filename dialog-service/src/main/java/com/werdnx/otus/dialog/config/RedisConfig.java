package com.werdnx.otus.dialog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {

    @Bean(name = "sendMessageScript")
    public RedisScript<Long> sendMessageScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/send_message.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean(name = "getDialogScript")
    public RedisScript<String> getDialogScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/get_dialog.lua"));
        script.setResultType(String.class);
        return script;
    }

}

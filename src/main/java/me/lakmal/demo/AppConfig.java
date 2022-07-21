package me.lakmal.demo;

import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create("redis://localhost:6379/0");
    }
}

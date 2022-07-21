package me.lakmal.demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;

import java.time.Duration;

public class RedisPubSub {
    public static void main(String[] args) {
        try (RedisClient client = RedisClient.create("redis://localhost:6379/0")) {

            StatefulRedisPubSubConnection<String, String> connection = client.connectPubSub();

            RedisPubSubReactiveCommands<String, String> reactive = connection.reactive();
            reactive.subscribe("channel1").subscribe();

            reactive.observeChannels()
                    .doOnNext(message -> System.out.println(message.getMessage()))
                    .blockLast(Duration.ofMinutes(3));

            connection.close();
            client.shutdown();
        }
    }
}

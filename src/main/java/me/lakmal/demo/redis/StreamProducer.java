package me.lakmal.demo.redis;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
public class StreamProducer {
    public static void main(String[] args) throws Exception {
        var client = RedisClient.create("redis://localhost:6379/0");
        var connection = client.connect();
        var commands = connection.sync();

        while (true) {
            var body = Collections.singletonMap("time", LocalDateTime.now().toString());
            log.info("Adding message with body {}", body);
            commands.xadd("my_stream", body);

            Thread.sleep(1000);
        }
    }
}

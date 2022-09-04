package me.lakmal.demo.redis;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class StreamProducer {
    public static void main(String[] args) throws Exception {
        var client = RedisClient.create("redis://localhost:6379/0");
        var connection = client.connect();
        var redisReactiveCommands = connection.sync();

        while (true) {
            //var body = Collections.singletonMap("time", LocalDateTime.now().toString());
            var msg = Map.of("docId", "1234");
            redisReactiveCommands.xadd("comment:participant_stream", msg);

            Thread.sleep(1000);
        }
    }
}

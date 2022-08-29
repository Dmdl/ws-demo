package me.lakmal.demo.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.XReadArgs;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class StreamConsumer {

    public static void main(String[] args) {
        var client = RedisClient.create("redis://localhost:6379/0");
        var connection = client.connect();
        var commands = connection.sync();

        var lastSeenMsg = "0-0";
        while (true) {
            var messages = commands.xread(XReadArgs.Builder.block(Duration.ofSeconds(1)), XReadArgs.StreamOffset.from("my_stream", lastSeenMsg));

            for (var msg : messages){
                lastSeenMsg = msg.getId();
                log.info(String.format("Received %s", msg));
            }
        }
    }
}

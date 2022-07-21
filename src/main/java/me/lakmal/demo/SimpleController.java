package me.lakmal.demo;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SimpleController {

    @Autowired
    private RedisClient redisClient;

    @PostMapping("publish")
    public String greet(@RequestBody Message message) {
        redisClient
                .connectPubSub()
                .reactive()
                .publish(message.getChannel(), message.getMessage())
                .subscribe();
        return "success";
    }
}

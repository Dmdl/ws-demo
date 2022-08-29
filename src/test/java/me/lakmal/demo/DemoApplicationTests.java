package me.lakmal.demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.XReadArgs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    RedisClient redisClient;

//    private static RSocketRequester requester;
//
//    @BeforeAll
//    public static void setupOnce(@Autowired RSocketRequester.Builder builder,
//                                 @LocalRSocketServerPort Integer port,
//                                 @Autowired RSocketStrategies strategies) {
//        requester = builder.connectTcp("localhost", port)
//                .block();
//    }
//
//    @Test
//    void testRequestResponse() {
//        var mono = requester.route("request-response")
//                .data(new Message("Hello"))
//                .retrieveMono(Message.class);
//
//        StepVerifier.create(mono)
//                .consumeNextWith(m -> Assertions.assertEquals(m.getMessage(), "Hello Hello"))
//                .verifyComplete();
//    }

    @Test
    public void test() throws Exception{
//        redisClient.connectPubSub().reactive().xread(new XReadArgs().block(2000),XReadArgs.StreamOffset.from("some-stream", "0")).subscribe(msg -> {
//            System.out.println(msg);
//        });

        AtomicInteger elementsSeen = new AtomicInteger(0);
        redisClient.connectPubSub().reactive()
                .xread(
                        new XReadArgs().block(2000),
                        XReadArgs.StreamOffset.from("some-stream", "0")
                )
                .subscribe(stringStringStreamMessage -> {
                    elementsSeen.incrementAndGet();
                });

        Thread.sleep(500);

        Assertions.assertEquals(2, elementsSeen.get());

        Thread.sleep(500);
    }

}

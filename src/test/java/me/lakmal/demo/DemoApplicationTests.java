package me.lakmal.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import reactor.test.StepVerifier;

@SpringBootTest
class DemoApplicationTests {

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

}

package me.lakmal.demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@SpringBootApplication
public class RequestResponse {
    public static void main(String[] args) {
        SpringApplication.run(RequestResponse.class, args);
    }
}

@Slf4j
@Component
class Producer implements Ordered, ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisClient redisClient;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        RSocketServer.create(SocketAcceptor.forRequestStream(handler -> subscribeToRedis()
                        .map(DefaultPayload::create)))
//                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .payloadDecoder(PayloadDecoder.DEFAULT)
//                .bind(TcpServerTransport.create(7000))
                .bind(WebsocketServerTransport.create(7000))
                .subscribe();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    Flux<String> subscribeToRedis() {
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();

        RedisPubSubReactiveCommands<String, String> reactive = connection.reactive();
        reactive.subscribe("channel1").subscribe();

        reactive.observeChannels()
                .doOnNext(patternMessage -> sink.tryEmitNext(patternMessage.getMessage()))
                .subscribe();
        return sink.asFlux();
    }
}

@Slf4j
@Component
class Consumer implements Ordered, ApplicationListener<ApplicationReadyEvent> {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        RSocketConnector.create()
                .payloadDecoder(PayloadDecoder.DEFAULT)
//                .connect(TcpClientTransport.create(7000))
                .connect(WebsocketClientTransport.create(7000))
                .flatMapMany(sender -> sender
                        .requestStream(DefaultPayload.create("To server >>"))
                        .map(Payload::getDataUtf8))
                .subscribe(result -> log.info("new message >> " + result));
    }
}

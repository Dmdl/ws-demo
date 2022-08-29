package me.lakmal.demo;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import me.lakmal.demo.db.Comment;
import me.lakmal.demo.db.Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Optional;

@SpringBootApplication
@EnableTransactionManagement
public class RequestResponse {
    public static void main(String[] args) {
        SpringApplication.run(RequestResponse.class, args);
    }
}

@Slf4j
@Component
class Producer implements Ordered, ApplicationListener<ApplicationReadyEvent> {

    private final RedisClient redisClient;
    private final Repository repository;

    public Producer(RedisClient redisClient, Repository repository) {
        this.redisClient = redisClient;
        this.repository = repository;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        RSocketServer.create(SocketAcceptor.forRequestStream(handler -> subscribeToStream()
                        .map(DefaultPayload::create)))
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
//                .payloadDecoder(PayloadDecoder.DEFAULT)
//                .bind(TcpServerTransport.create(7000))
                .bind(WebsocketServerTransport.create(7000))
                .subscribe();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    Flux<String> subscribeToPubSub() {
//        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();

        RedisPubSubReactiveCommands<String, String> reactive = connection.reactive();
        reactive.subscribe("channel1").subscribe();

        reactive.observeChannels()
                .doOnNext(msg -> {
                    Optional<Comment> comment = repository.findById(msg.getMessage());
                    sink.tryEmitNext(new Gson().toJson(comment.get()));
                })
                .subscribe();
        return sink.asFlux();
    }

    Flux<String> subscribeToStream() {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();

        RedisPubSubReactiveCommands<String, String> reactive = connection.reactive();

        reactive.xread(new XReadArgs().block(Duration.ofMinutes(5)), XReadArgs.StreamOffset.from("some-stream", "$"))
                .doOnNext(msg -> sink.tryEmitNext(msg.getBody().get("key")))
                .repeat()
                .doOnError(e -> log.info("on error >>> {}", e.getMessage()))
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
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
//                .connect(TcpClientTransport.create(7000))
                .connect(WebsocketClientTransport.create(7000))
                .flatMapMany(sender -> sender
                        .requestStream(DefaultPayload.create("To server >>"))
                        .map(Payload::getDataUtf8))
                .subscribe(result -> log.info("new message >> " + result));
    }
}

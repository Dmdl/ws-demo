package me.lakmal.demo.channel;

import io.lettuce.core.RedisClient;
import io.lettuce.core.XReadArgs;
import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class Channel implements ApplicationListener<ApplicationReadyEvent> {

    private final RedisClient redisClient;

    private static final String STREAM_KEY = "some-stream";
    private final Sinks.Many<String> SINK = Sinks.many().multicast().onBackpressureBuffer();

    public Channel(RedisClient redisClient) {
        this.redisClient = redisClient;
        emitMessage();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        RSocketServer
                .create(SocketAcceptor.forRequestChannel(in -> subscribeToStream(in).map(DefaultPayload::create)))
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .bind(WebsocketServerTransport.create(7001))
                .subscribe();
    }

    private Flux<String> subscribeToStream(Publisher<Payload> streamIn) {

        var connection = redisClient.connectPubSub();
        var reactive = connection.reactive();

        Flux.from(streamIn)
                .doOnNext(msg -> {
                    log.info("Incoming >> {}", msg.getDataUtf8());
                    reactive.xadd(STREAM_KEY, Map.of("key", msg.getDataUtf8())).subscribe();
                })
                .subscribe();
        return SINK.asFlux();
    }

    private void emitMessage() {
        var connection = redisClient.connectPubSub();
        var reactive = connection.reactive();

        var lastId = new AtomicReference<>("$");
        reactive.xread(new XReadArgs().block(Duration.ofSeconds(10)), XReadArgs.StreamOffset.from(STREAM_KEY, lastId.get()))
                .doOnNext(msg -> {
                    log.info("From Redis >>> {}", msg.getBody().get("key"));
                    SINK.tryEmitNext(msg.getBody().get("key"));
                    lastId.set(msg.getId());
                })
                .repeat()
                .doOnError(e -> log.info("on error >>> {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.error("Error >> {} ", e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }
}

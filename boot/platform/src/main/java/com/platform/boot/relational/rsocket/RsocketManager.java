package com.platform.boot.relational.rsocket;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Service
public class RsocketManager {

    private final Sinks.Many<MessageOut> replaySink = Sinks.many().replay().limit(Duration.ofMinutes(5));
    private final ConcurrentHashMap<String, ConnectedClient> clients = new ConcurrentHashMap<>(200);

    public void connect(String clientIdentifier, RSocketRequester requester) {
        log.debug("Connect [{}] RSocketRequester.", clientIdentifier);
        Objects.requireNonNull(requester.rsocket(), "rsocket connection should not be null")
                .onClose()
                .doFirst(() -> this.clients.put(clientIdentifier, new ConnectedClient(requester)))
                .doFinally(sig -> {
                    log.debug("Client closed, uuid is {}. signal is {}.", clientIdentifier, sig.toString());
                    this.clients.remove(clientIdentifier);
                }).subscribe();
    }

    public Flux<MessageOut> radars(String clientIdentifier, RSocketRequester requester) {
        log.debug("Radars [{}] RSocketRequester.", clientIdentifier);
        this.clients.put(clientIdentifier, new ConnectedClient(requester));
        return this.replaySink.asFlux();
    }

    public void send(MessageIn message) {
        MessageOut messageOut = MessageOut.of(message.getType(), message.getContent(), message.getData());
        try {
            this.replaySink.tryEmitNext(messageOut.status(200));
        } catch (Exception e) {
            log.error("send message error : {}", e.getMessage());
            this.replaySink.tryEmitNext(messageOut.status(500).content(e.getMessage()));
        }
    }

    public void taskTest() {
        if (this.clients.isEmpty() || !this.clients.containsKey("CommandClient")) {
            return;
        }
        ConnectedClient connectedClient = this.clients.get("CommandClient");
        connectedClient.getRequester().route("user.message")
                .data(MessageOut.of(MessageType.COMMAND, "test", "test").status(200))
                .send().subscribe();
    }
}

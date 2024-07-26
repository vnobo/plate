package com.plate.boot.relational.rsocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Controller
@RequiredArgsConstructor
public class RsocketController {

    private final RsocketManager rsocketManager;

    @ConnectMapping("connect.setup")
    public Mono<Void> setup(String clientIdentifier, RSocketRequester requester) {
        Objects.requireNonNull(requester.rsocket(), "rsocket connection should not be null");
        this.rsocketManager.connect(clientIdentifier, requester);
        return Mono.empty();
    }

    @MessageMapping("request.stream")
    public Flux<MessageOut> stream(String clientIdentifier, RSocketRequester requester) {
        return this.rsocketManager.radars(clientIdentifier, requester);
    }

    @MessageMapping("request.sender")
    public Mono<MessageOut> sender(Mono<MessageIn> messageInMono) {
        return messageInMono.doOnNext(this.rsocketManager::send)
                .map(in -> MessageOut.of(in.getType(), in.getContent(), in.getData()).status(200));
    }

}

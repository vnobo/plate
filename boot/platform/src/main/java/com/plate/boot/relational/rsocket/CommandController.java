package com.plate.boot.relational.rsocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@RestController
@RequestMapping("/command/v1")
@RequiredArgsConstructor
public class CommandController {

    private final RSocketRequester rSocketRequester;

    @PostMapping("send")
    public Mono<MessageOut> send(@RequestBody CommandRequest command) {
        var dataFlux = Mono.just(MessageIn.of(command.getType(), command.getCommand(), null));
        return this.rSocketRequester.route("request.sender")
                .data(dataFlux).retrieveMono(MessageOut.class);
    }

}

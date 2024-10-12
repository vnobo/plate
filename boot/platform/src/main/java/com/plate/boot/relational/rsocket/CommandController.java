package com.plate.boot.relational.rsocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 控制器类，用于处理命令相关的HTTP请求.
 * 通过RSocket进行通信，实现命令的发送与处理.
 */
@RestController
@RequestMapping("/command/v1")
@RequiredArgsConstructor
public class CommandController {

    // RSocket请求器，用于发送RSocket请求
    private final RSocketRequester rSocketRequester;

    /**
     * 处理发送命令的请求.
     *
     * @param command 从请求体中获取的命令请求对象，包含命令类型和具体命令.
     * @return 返回一个Mono流，包含发送命令后的响应消息.
     */
    @PostMapping("send")
    public Mono<MessageOut> send(@RequestBody CommandRequest command) {
        // 创建一个包含命令信息的消息流
        var dataFlux = Mono.just(MessageIn.of(command.getType(), command.getCommand(), null));
        // 通过RSocket请求器发送消息，并期望返回一个MessageOut类型的响应
        return this.rSocketRequester.route("request.sender")
                .data(dataFlux).retrieveMono(MessageOut.class);
    }
}


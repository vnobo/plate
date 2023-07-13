package com.platform.boot.security.captcha;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final ServerCaptchaRepository captchaTokenRepository = new ServerCaptchaRepository();

    @GetMapping("code")
    public Mono<ResponseEntity<DataBuffer>> getCaptcha(ServerWebExchange exchange) {
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer(2048);
        return captchaTokenRepository.loadToken(exchange).flatMap(captchaToken -> {
            try (OutputStream ignored = dataBuffer.asOutputStream()) {
                //captchaToken.getCaptcha().write(outputStream);
                return Mono.just(ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(dataBuffer))
                        .delayUntil((a) -> this.captchaTokenRepository.saveToken(exchange, captchaToken));
            } catch (IOException e) {
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(dataBuffer.read(e.getMessage().getBytes(StandardCharsets.UTF_8))));
            }
        });
    }

}
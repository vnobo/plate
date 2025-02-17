package com.plate.boot.security.captcha;

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
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * REST controller responsible for handling captcha generation and validation operations.
 * <p>
 * This controller exposes endpoints to generate captcha images and manage captcha tokens within the application's sessions.
 * <p>
 * The class is annotated with \@RestController to indicate that it's a REST controller in the Spring context.
 * It is also annotated with \@RequestMapping to map HTTP requests to handler methods.
 * \@RequiredArgsConstructor is used to generate a constructor with required arguments.
 * <p>
 * The controller uses reactive programming with Project Reactor.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaRepository captchaTokenRepository;

    /**
     * Generates a captcha image and returns it as a response entity.
     * <p>
     * This endpoint handles GET requests to "/captcha/code".
     * It generates a captcha token, converts it to a ByteArrayResource, and returns it as a PNG image.
     * The token is also saved in the repository.
     *
     * @param exchange the server web exchange
     * @return a Mono emitting the response entity containing the captcha image
     */
    @GetMapping("code")
    public Mono<ResponseEntity<DataBuffer>> getCaptcha(ServerWebExchange exchange) {
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer(2048);
        return this.captchaTokenRepository.generateToken(exchange)
                .publishOn(Schedulers.boundedElastic()).flatMap(captchaToken -> {
                    try (OutputStream outputStream = dataBuffer.asOutputStream()) {
                        outputStream.write(captchaToken.captcha().getBytes());
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
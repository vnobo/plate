package com.plate.boot.security.captcha;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CaptchaController}.
 */
@ExtendWith(MockitoExtension.class)
class CaptchaControllerTest {

    @Mock
    private CaptchaRepository captchaTokenRepository;

    @InjectMocks
    private CaptchaController controller;

    private static CaptchaToken captchaToken() {
        return new CaptchaToken("X-CAPTCHA-TOKEN", "_captcha", "12345");
    }

    @Test
    void getCaptchaReturnsPngImageAndSavesToken() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());

        CaptchaToken token = captchaToken();
        when(captchaTokenRepository.generateToken(exchange)).thenReturn(Mono.just(token));
        when(captchaTokenRepository.saveToken(exchange, token)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getCaptcha(exchange))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
                    DataBuffer body = entity.getBody();
                    byte[] bytes = new byte[body.readableByteCount()];
                    body.read(bytes);
                    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("12345");
                })
                .verifyComplete();

        verify(captchaTokenRepository).saveToken(exchange, token);
    }

    @Test
    void getCaptchaReturnsInternalServerErrorOnWriteFailure() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        DataBufferFactory bufferFactory = mock(DataBufferFactory.class);
        DataBuffer dataBuffer = mock(DataBuffer.class);
        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(bufferFactory);
        when(bufferFactory.allocateBuffer(2048)).thenReturn(dataBuffer);

        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("boom");
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                throw new IOException("boom");
            }
        };
        when(dataBuffer.asOutputStream()).thenReturn(outputStream);
        when(dataBuffer.read(any(byte[].class))).thenReturn(dataBuffer);

        CaptchaToken token = captchaToken();
        when(captchaTokenRepository.generateToken(exchange)).thenReturn(Mono.just(token));

        StepVerifier.create(controller.getCaptcha(exchange))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(entity.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
                })
                .verifyComplete();

        verify(captchaTokenRepository, never()).saveToken(any(), any());
    }
}

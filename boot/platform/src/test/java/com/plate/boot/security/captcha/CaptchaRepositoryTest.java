package com.plate.boot.security.captcha;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CaptchaRepository#createCaptchaToken()}, verifying that the generated
 * captcha code is randomly produced (no longer the previously hardcoded {@code "54321"}) and that
 * successive codes vary.
 */
class CaptchaRepositoryTest {

    @Test
    void createCaptchaTokenShouldGenerateRandomFiveDigitCodeNotHardcoded() {
        CaptchaRepository repository = new CaptchaRepository();

        CaptchaToken token = repository.createCaptchaToken();

        assertThat(token.captcha()).isNotEqualTo("54321");
        assertThat(token.captcha()).matches("\\d{5}");
    }

    @Test
    void createCaptchaTokenShouldVaryAcrossCalls() {
        CaptchaRepository repository = new CaptchaRepository();

        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            codes.add(repository.createCaptchaToken().captcha());
        }

        assertThat(codes.size()).isGreaterThan(1);
    }

    @Test
    void generateTokenPutsTokenIntoExchangeAttributes() {
        CaptchaRepository repository = new CaptchaRepository();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        Map<String, Object> attributes = new HashMap<>();
        when(exchange.getAttributes()).thenReturn(attributes);

        CaptchaToken token = repository.generateToken(exchange).block();

        assertThat(token).isNotNull();
        assertThat(token.captcha()).matches("\\d{5}");
        assertThat(attributes.get(CaptchaToken.class.getName())).isSameAs(token);
    }

    @Test
    void saveTokenStoresTokenInSessionAndRefreshesSessionId() {
        CaptchaRepository repository = new CaptchaRepository();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        WebSession session = mock(WebSession.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(session.getAttributes()).thenReturn(sessionAttributes);
        when(session.changeSessionId()).thenReturn(Mono.empty());

        CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "12345");
        repository.saveToken(exchange, token).block();

        assertThat(sessionAttributes.get(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME)).isSameAs(token);
        verify(session).changeSessionId();
    }

    @Test
    void clearTokenRemovesTokenFromSessionAndRefreshesSessionId() {
        CaptchaRepository repository = new CaptchaRepository();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        WebSession session = mock(WebSession.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME,
                CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "12345"));
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(session.getAttributes()).thenReturn(sessionAttributes);
        when(session.changeSessionId()).thenReturn(Mono.empty());

        repository.clearToken(exchange).block();

        assertThat(sessionAttributes).doesNotContainKey(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME);
        verify(session).changeSessionId();
    }

    @Test
    void loadTokenReturnsTokenWhenPresent() {
        CaptchaRepository repository = new CaptchaRepository();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        WebSession session = mock(WebSession.class);
        CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "12345");
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(session.getAttributes()).thenReturn(
                Map.of(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, token));
        when(session.getAttribute(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME)).thenReturn(token);

        assertThat(repository.loadToken(exchange).block()).isSameAs(token);
    }

    @Test
    void loadTokenReturnsEmptyWhenAbsent() {
        CaptchaRepository repository = new CaptchaRepository();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        WebSession session = mock(WebSession.class);
        when(exchange.getSession()).thenReturn(Mono.just(session));
        when(session.getAttributes()).thenReturn(new HashMap<>());

        assertThat(repository.loadToken(exchange).block()).isNull();
    }
}

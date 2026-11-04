package com.plate.boot.security.captcha;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static com.plate.boot.security.captcha.CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CaptchaFilter} and its inner {@link CaptchaFilter.CaptchaException}
 * and {@link CaptchaFilter.CaptchaServerAccessDeniedHandler} classes.
 */
class CaptchaFilterTest {

    private static final String CAPTCHA_HEADER = "X-CAPTCHA-TOKEN";

    private final CaptchaRepository captchaRepository = mock(CaptchaRepository.class);

    private final CaptchaFilter filter = new CaptchaFilter(captchaRepository);

    @Test
    void filterContinuesChainWhenPathDoesNotRequireCaptcha() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/captcha/code").build());
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void filterContinuesChainWhenSessionHasNoCaptchaToken() {
        MockServerWebExchange exchange = exchangeWithSession(new HashMap<>(), null);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void filterDeniesAccessWhenCaptchaTokenCannotBeLoaded() {
        MockServerWebExchange exchange = exchangeWithSession(Map.of(DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, "token"), null);
        when(captchaRepository.loadToken(exchange)).thenReturn(Mono.empty());
        when(captchaRepository.clearToken(exchange)).thenReturn(Mono.empty());
        WebFilterChain chain = mock(WebFilterChain.class);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void filterDeniesAccessWhenCaptchaHeaderMissing() {
        CaptchaToken token = CaptchaToken.of(CAPTCHA_HEADER, "_captcha", "12345");
        MockServerWebExchange exchange = exchangeWithSession(Map.of(DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, token), null);
        when(captchaRepository.loadToken(exchange)).thenReturn(Mono.just(token));
        when(captchaRepository.clearToken(exchange)).thenReturn(Mono.empty());
        WebFilterChain chain = mock(WebFilterChain.class);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void filterDeniesAccessWhenCaptchaHeaderInvalid() {
        CaptchaToken token = CaptchaToken.of(CAPTCHA_HEADER, "_captcha", "12345");
        MockServerWebExchange exchange = exchangeWithSession(Map.of(DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, token), "00000");
        when(captchaRepository.loadToken(exchange)).thenReturn(Mono.just(token));
        when(captchaRepository.clearToken(exchange)).thenReturn(Mono.empty());
        WebFilterChain chain = mock(WebFilterChain.class);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void filterContinuesChainWhenCaptchaIsValid() {
        CaptchaToken token = CaptchaToken.of(CAPTCHA_HEADER, "_captcha", "12345");
        MockServerWebExchange exchange = exchangeWithSession(Map.of(DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, token), "12345");
        when(captchaRepository.loadToken(exchange)).thenReturn(Mono.just(token));
        when(captchaRepository.clearToken(exchange)).thenReturn(Mono.empty());
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(captchaRepository).clearToken(exchange);
        verify(chain).filter(exchange);
    }

    @Test
    void getOrderReturnsHighestPrecedence() {
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void captchaExceptionCarriesMessageAndIsAccessDenied() {
        CaptchaFilter.CaptchaException exception = new CaptchaFilter.CaptchaException("boom");

        assertThat(exception).isInstanceOf(AccessDeniedException.class);
        assertThat(exception.getMessage()).isEqualTo("boom");
    }

    @Test
    void accessDeniedHandlerRejectsNullStatus() {
        assertThatThrownBy(() -> new CaptchaFilter.CaptchaServerAccessDeniedHandler(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accessDeniedHandlerWritesJsonResponse() {
        CaptchaFilter.CaptchaServerAccessDeniedHandler handler =
                new CaptchaFilter.CaptchaServerAccessDeniedHandler(HttpStatus.FORBIDDEN);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/oauth2/token").build());
        AccessDeniedException exception = new AccessDeniedException("Captcha is invalid");

        StepVerifier.create(handler.handle(exchange, exception)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("Captcha authentication failed");
        assertThat(body).contains("Captcha is invalid");
    }

    private MockServerWebExchange exchangeWithSession(Map<String, Object> attributes, String captchaHeader) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder =
                MockServerHttpRequest.post("/oauth2/token");
        if (captchaHeader != null) {
            requestBuilder.header(CAPTCHA_HEADER, captchaHeader);
        }
        WebSession session = mock(WebSession.class);
        when(session.getAttributes()).thenReturn(attributes);
        return MockServerWebExchange.builder(requestBuilder.build()).session(session).build();
    }
}

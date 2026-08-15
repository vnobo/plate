package com.plate.boot.config;

import com.plate.boot.commons.exception.RestServerException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.lang.reflect.Field;
import java.util.List;

import static com.plate.boot.config.SessionConfiguration.HEADER_SESSION_ID_NAME;
import static com.plate.boot.config.SessionConfiguration.X_REQUESTED_WITH;
import static com.plate.boot.config.SessionConfiguration.XML_HTTP_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SessionConfiguration} and its inner
 * {@link SessionConfiguration.CustomWebSessionIdResolver}.
 */
class SessionConfigurationTest {

    @Test
    void webSessionIdResolverBeanUsesCustomHeaderName() {
        SessionConfiguration configuration = new SessionConfiguration();

        WebSessionIdResolver resolver = configuration.webSessionIdResolver();

        assertThat(resolver).isInstanceOf(SessionConfiguration.CustomWebSessionIdResolver.class);
        assertThat(((HeaderWebSessionIdResolver) resolver).getHeaderName()).isEqualTo(HEADER_SESSION_ID_NAME);
    }

    @Test
    void setSessionIdWritesHeaderWhenRequestedWithHeaderPresent() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST).build());

        resolver.setSessionId(exchange, "session-123");

        assertThat(exchange.getResponse().getHeaders().getFirst(HEADER_SESSION_ID_NAME)).isEqualTo("session-123");
    }

    @Test
    void setSessionIdDelegatesToCookieResolverWhenRequestedWithHeaderAbsent() {
        CookieWebSessionIdResolver cookieResolver = mock(CookieWebSessionIdResolver.class);
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(cookieResolver);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());

        resolver.setSessionId(exchange, "session-123");

        verify(cookieResolver).setSessionId(exchange, "session-123");
    }

    @Test
    void resolveSessionIdsUsesBearerTokenForXmlHttpRequest() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .header("Authorization", "Bearer abc123")
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("abc123");
    }

    @Test
    void resolveSessionIdsAcceptsBearerTokenCaseInsensitive() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .header("Authorization", "bearer xyz-9._~+/=")
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("xyz-9._~+/=");
    }

    @Test
    void resolveSessionIdsFallsBackToHeaderWhenNoTokenForXmlHttpRequest() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .header(HEADER_SESSION_ID_NAME, "header-session")
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("header-session");
    }

    @Test
    void resolveSessionIdsReturnsEmptyWhenNoTokenAndNoHeaderForXmlHttpRequest() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).isEmpty();
    }

    @Test
    void resolveSessionIdsUsesCookieResolverForNonXmlHttpRequest() {
        CookieWebSessionIdResolver cookieResolver = mock(CookieWebSessionIdResolver.class);
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(cookieResolver);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path").build());
        when(cookieResolver.resolveSessionIds(exchange)).thenReturn(List.of("cookie-session"));

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("cookie-session");
    }

    @Test
    void resolveSessionIdsUsesCookieResolverWhenRequestedWithIsNotXmlHttpRequest() {
        CookieWebSessionIdResolver cookieResolver = mock(CookieWebSessionIdResolver.class);
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(cookieResolver);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, "fetch")
                .build());
        when(cookieResolver.resolveSessionIds(exchange)).thenReturn(List.of("cookie-session"));

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("cookie-session");
    }

    @Test
    void resolveSessionIdsUsesParameterTokenForGetRequest() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .queryParam("access_token", "param-token")
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("param-token");
    }

    @Test
    void resolveSessionIdsIgnoresParameterTokenForNonGetRequest() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .queryParam("access_token", "param-token")
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).isEmpty();
    }

    @Test
    void resolveSessionIdsThrowsWhenMultipleParameterTokens() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .queryParam("access_token", "token-a", "token-b")
                .build());

        assertThatThrownBy(() -> resolver.resolveSessionIds(exchange))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void resolveSessionIdsThrowsWhenAuthorizationAndParameterTokensBothPresent() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .header("Authorization", "Bearer abc123")
                .queryParam("access_token", "param-token")
                .build());

        assertThatThrownBy(() -> resolver.resolveSessionIds(exchange))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void resolveSessionIdsThrowsWhenBearerTokenMalformed() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .header("Authorization", "Bearer not valid token")
                .build());

        assertThatThrownBy(() -> resolver.resolveSessionIds(exchange))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void resolveSessionIdsIgnoresNonBearerAuthorizationHeader() {
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .header(X_REQUESTED_WITH, XML_HTTP_REQUEST)
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).isEmpty();
    }

    @Test
    void expireSessionDelegatesToCookieResolver() {
        CookieWebSessionIdResolver cookieResolver = mock(CookieWebSessionIdResolver.class);
        SessionConfiguration.CustomWebSessionIdResolver resolver = newResolver(cookieResolver);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path").build());

        resolver.expireSession(exchange);

        verify(cookieResolver).expireSession(exchange);
    }

    @Test
    void resolveSessionIdsReadsCookieWhenCookieResolverIsReal() {
        SessionConfiguration.CustomWebSessionIdResolver resolver =
                new SessionConfiguration.CustomWebSessionIdResolver();
        resolver.setHeaderName(HEADER_SESSION_ID_NAME);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path")
                .cookie(new HttpCookie("SESSION", "real-cookie-id"))
                .build());

        assertThat(resolver.resolveSessionIds(exchange)).containsExactly("real-cookie-id");
    }

    private SessionConfiguration.CustomWebSessionIdResolver newResolver(CookieWebSessionIdResolver cookieResolver) {
        SessionConfiguration.CustomWebSessionIdResolver resolver =
                new SessionConfiguration.CustomWebSessionIdResolver();
        resolver.setHeaderName(HEADER_SESSION_ID_NAME);
        if (cookieResolver != null) {
            injectCookieResolver(resolver, cookieResolver);
        }
        return resolver;
    }

    private void injectCookieResolver(SessionConfiguration.CustomWebSessionIdResolver resolver,
                                      CookieWebSessionIdResolver cookieResolver) {
        try {
            Field field = SessionConfiguration.CustomWebSessionIdResolver.class
                    .getDeclaredField("cookieWebSessionIdResolver");
            field.setAccessible(true);
            field.set(resolver, cookieResolver);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to inject cookie resolver", e);
        }
    }
}

package com.platform.boot.security.oauth2;

import com.platform.boot.commons.exception.RestServerException;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.core.AuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.platform.boot.config.SessionConfiguration.XML_HTTP_REQUEST;
import static com.platform.boot.config.SessionConfiguration.X_REQUESTED_WITH;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Component
public class Oauth2SuccessHandler extends RedirectServerAuthenticationSuccessHandler {

    private static final MediaType APPLICATION_JSON_TYPE = MediaType.APPLICATION_JSON;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw RestServerException.withMsg("Authentication token must be an instance of OAuth2AuthenticationToken",
                    List.of());
        }

        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        String requestedWith = request.getHeaders().getFirst(X_REQUESTED_WITH);

        if (isXmlHttpRequest(requestedWith)) {
            return handleXmlHttpRequest(exchange, (OAuth2AuthenticationToken) authentication);
        }
        return super.onAuthenticationSuccess(webFilterExchange, authentication);
    }

    private boolean isXmlHttpRequest(String requestedWith) {
        return requestedWith != null && requestedWith.contains(XML_HTTP_REQUEST);
    }

    private Mono<Void> handleXmlHttpRequest(ServerWebExchange exchange, OAuth2AuthenticationToken token) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(APPLICATION_JSON_TYPE);

        return exchange.getSession()
                .flatMap(session -> {
                    AuthenticationToken authenticationToken = AuthenticationToken.build(session, token);
                    return writeAuthenticationToken(response, authenticationToken);
                });
    }

    private Mono<Void> writeAuthenticationToken(ServerHttpResponse response, AuthenticationToken authenticationToken) {
        var body = ContextUtils.objectToBytes(authenticationToken);
        var dataBufferFactory = response.bufferFactory();
        var bodyBuffer = dataBufferFactory.wrap(body);
        return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged());
    }
}
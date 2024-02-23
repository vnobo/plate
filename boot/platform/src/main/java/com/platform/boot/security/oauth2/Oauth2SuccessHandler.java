package com.platform.boot.security.oauth2;

import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.core.AuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Component
public class Oauth2SuccessHandler extends RedirectServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        String xRequestedWith = "X-Requested-With";
        String requestedWith = request.getHeaders().getFirst(xRequestedWith);
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String xmlHttpRequest = "XMLHttpRequest";
        if (requestedWith != null && requestedWith.contains(xmlHttpRequest)) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getSession().flatMap(session -> {
                AuthenticationToken authenticationToken = AuthenticationToken.build(session, token);
                var body = ContextUtils.objectToBytes(authenticationToken);
                var dataBufferFactory = response.bufferFactory();
                var bodyBuffer = dataBufferFactory.wrap(body);
                return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged());
            });
        }
        return super.onAuthenticationSuccess(webFilterExchange, authentication);
    }
}
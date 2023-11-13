package com.platform.boot.security;

import com.platform.boot.commons.utils.ContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
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
public class Oauth2AuthenticationSuccessHandler extends RedirectServerAuthenticationSuccessHandler {
    private final String xRequestedWith = "X-Requested-With";
    private final String xmlHttpRequest = "XMLHttpRequest";

    private final ReactiveUserDetailsService securityManager;

    public Oauth2AuthenticationSuccessHandler(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        String requestedWith = request.getHeaders().getFirst(xRequestedWith);
        if (requestedWith != null && requestedWith.contains(xmlHttpRequest)) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return this.securityManager.findByUsername(authentication.getName())
                    .flatMap(userDetails -> exchange.getSession()).flatMap(session -> {
                        AuthenticationToken authenticationToken = AuthenticationToken.build(session);
                        var body = ContextUtils.objectToBytes(authenticationToken);
                        var dataBufferFactory = response.bufferFactory();
                        var bodyBuffer = dataBufferFactory.wrap(body);
                        return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged());
                    });
        }
        return super.onAuthenticationSuccess(webFilterExchange, authentication);
    }
}
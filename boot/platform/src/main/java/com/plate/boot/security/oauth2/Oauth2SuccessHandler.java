package com.plate.boot.security.oauth2;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.security.AuthenticationToken;
import org.springframework.core.io.buffer.DataBuffer;
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

import static com.plate.boot.config.SessionConfiguration.XML_HTTP_REQUEST;
import static com.plate.boot.config.SessionConfiguration.X_REQUESTED_WITH;

/**
 * Oauth2SuccessHandler is a component that extends the functionality of RedirectServerAuthenticationSuccessHandler,
 * specifically tailored for handling successful OAuth2 authentication events within a web application.
 * Upon a successful authentication, it differentiates between regular and XMLHttpRequests (XHR) to provide
 * an appropriate response. For XHRs, it constructs an {@link AuthenticationToken}, serializes it into JSON,
 * and sends it back with a 200 OK status, whereas non-XHR requests follow default success handling.
 *
 * <p>This handler ensures that the incoming authentication is of type OAuth2AuthenticationToken before proceeding
 * and throws a {@link RestServerException} if not met. It utilizes session data to build the authentication token,
 * making it suitable for applications requiring session management alongside OAuth2 authentication flows.
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *     <li>Detects and handles XHR responses distinctively from regular page redirects.</li>
 *     <li>Serializes an {@link AuthenticationToken} to JSON for AJAX-based clients.</li>
 *     <li>Enforces OAuth2AuthenticationToken type-checking for incoming authentications.</li>
 *     <li>Utilizes the current session to construct a session-aware authentication token.</li>
 * </ul>
 * @see OAuth2AuthenticationToken For the type of authentication tokens expected by this handler.
 * @see AuthenticationToken For the structure representing serialized authentication details.
 * @see RedirectServerAuthenticationSuccessHandler For the base class functionality.
 */
@Component
public class Oauth2SuccessHandler extends RedirectServerAuthenticationSuccessHandler {

    /**
     * Static constant representing the media type for JSON (application/json) requests and responses.
     * This is used to set the content type of HTTP messages when working with JSON data.
     */
    private static final MediaType APPLICATION_JSON_TYPE = MediaType.APPLICATION_JSON;

    /**
     * Handles successful authentication events by either responding to XML HTTP Requests (XHR) or delegating to the superclass
     * handler based on the request type. If the authentication is not an instance of OAuth2AuthenticationToken, a RestServerException
     * is thrown.
     *
     * @param webFilterExchange The current WebFilterExchange containing the ServerWebExchange and SecurityContext.
     * @param authentication    The authentication result after a successful authentication attempt.
     * @return A Mono that, when subscribed to, completes without emitting any item for a successful void operation,
     * or signals an error if the authentication token is not an OAuth2AuthenticationToken.
     * @throws RestServerException if the authentication token is not an instance of OAuth2AuthenticationToken.
     */
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw RestServerException.withMsg(
                    "Authentication token must be an instance of OAuth2AuthenticationToken",
                    new TypeNotPresentException("OAuth2AuthenticationToken", null));
        }

        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        String requestedWith = request.getHeaders().getFirst(X_REQUESTED_WITH);

        if (isXmlHttpRequest(requestedWith)) {
            return handleXmlHttpRequest(exchange, (OAuth2AuthenticationToken) authentication);
        }
        return super.onAuthenticationSuccess(webFilterExchange, authentication);
    }

    /**
     * Determines whether the given request header indicates an XML HTTP Request (XHR).
     *
     * @param requestedWith The value of the 'X-Requested-With' header from the HTTP request.
     * @return {@code true} if the request is identified as an XHR, otherwise {@code false}.
     */
    private boolean isXmlHttpRequest(String requestedWith) {
        return requestedWith != null && requestedWith.contains(XML_HTTP_REQUEST);
    }

    /**
     * Handles an XML HTTP Request (XHR) by setting up the response with status and content type, then writes an
     * {@link AuthenticationToken} to the response. This method is designed to operate within a reactive environment,
     * utilizing ServerWebExchange and returning a Mono<Void> to signal completion.
     *
     * @param exchange The current ServerWebExchange which holds information about the HTTP request and response.
     * @param token The OAuth2AuthenticationToken instance obtained after a successful authentication.
     * @return A Mono<Void> that, when subscribed to, writes the authentication token to the response and completes,
     *         or handles any potential errors related to session retrieval or token writing.
     */
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

    /**
     * Writes the provided {@link AuthenticationToken} to the given {@link ServerHttpResponse}.
     * <p>
     * This method converts the authentication token into a byte array and wraps it in a {@link DataBuffer},
     * then writes this buffer to the response using a Flux that emits the single buffer and completes.
     * The response is flushed immediately after writing the buffer.
     *
     * @param response The ServerHttpResponse to which the authentication token will be written.
     * @param authenticationToken The authentication token to write to the response, containing details of the authentication.
     * @return A Mono<Void> that, when subscribed to, signals the completion of writing the authentication token to the response.
     */
    private Mono<Void> writeAuthenticationToken(ServerHttpResponse response, AuthenticationToken authenticationToken) {
        var body = BeanUtils.objectToBytes(authenticationToken);
        var dataBufferFactory = response.bufferFactory();
        var bodyBuffer = dataBufferFactory.wrap(body);
        return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged());
    }
}
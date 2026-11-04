package com.plate.boot.security.oauth2;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.UserReq;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Component-level test for {@link Oauth2UserService#loadUser(OAuth2UserRequest)}.
 * <p>
 * The overridden {@code loadUser} delegates to {@code super.loadUser(...)}, which performs a live
 * WebClient call to the configured {@code userInfoUri}. To cover that path without a real OAuth2
 * provider, this test boots an in-process {@link HttpServer} that serves the provider's userinfo
 * JSON, builds a {@link ClientRegistration} pointing at it, and mocks only the local
 * {@link SecurityManager} so that the local-user lookup falls back to the remote {@link OAuth2User}.
 */
class Oauth2UserServiceLoadUserTest {

    private HttpServer server;
    private JsonMapper savedMapper;

    @BeforeEach
    void setUp() throws Exception {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/userinfo", exchange -> {
            byte[] body = "{\"id\":123,\"login\":\"alice\",\"name\":\"Alice\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @Test
    void loadUserFetchesRemoteUserAndFallsBackToItWhenNoLocalUser() {
        int port = server.getAddress().getPort();

        ClientRegistration registration = ClientRegistration.withRegistrationId("github")
                .userInfoUri("http://localhost:" + port + "/userinfo")
                .userNameAttributeName("id")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user")
                .authorizationUri("http://localhost:" + port + "/oauth/authorize")
                .tokenUri("http://localhost:" + port + "/oauth/token")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600));

        OAuth2UserRequest userRequest = new OAuth2UserRequest(registration, accessToken);

        SecurityManager securityManager = mock(SecurityManager.class);
        when(securityManager.loadByOauth2(anyString(), anyString())).thenReturn(Mono.empty());
        when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.empty());

        Oauth2UserService service = new Oauth2UserService(securityManager);

        OAuth2User result = service.loadUser(userRequest).block();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("123");
        Object login = result.getAttribute("login");
        Object name = result.getAttribute("name");
        assertThat(login).isEqualTo("alice");
        assertThat(name).isEqualTo("Alice");
    }
}

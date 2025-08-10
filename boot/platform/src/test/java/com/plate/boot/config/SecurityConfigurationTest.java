package com.plate.boot.config;

import com.plate.boot.security.oauth2.Oauth2SuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * SecurityConfiguration Unit Tests
 *
 * <p>This test class provides unit tests for the SecurityConfiguration class, covering:</p>
 * <ul>
 *   <li>Password encoder bean creation</li>
 *   <li>OAuth2 client service bean creation</li>
 * </ul>
 *
 * @author Qwen Code
 */
class SecurityConfigurationTest {

    private SecurityConfiguration securityConfiguration;

    @BeforeEach
    void setUp() {
        Oauth2SuccessHandler authenticationSuccessHandler = mock(Oauth2SuccessHandler.class);
        securityConfiguration = new SecurityConfiguration(authenticationSuccessHandler);
    }

    @Nested
    @DisplayName("PasswordEncoder Tests")
    class PasswordEncoderTests {

        @Test
        @DisplayName("Should create delegating password encoder")
        void shouldCreateDelegatingPasswordEncoder() {
            // When
            PasswordEncoder passwordEncoder = securityConfiguration.passwordEncoder();

            // Then
            assertThat(passwordEncoder).isNotNull();
            // Verify it's a delegating password encoder by testing encoding
            String encoded = passwordEncoder.encode("testPassword");
            assertThat(encoded).startsWith("{bcrypt}");
            assertThat(passwordEncoder.matches("testPassword", encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("OAuth2 Client Service Tests")
    class OAuth2ClientServiceTests {

        @Test
        @DisplayName("Should create OAuth2 authorized client service")
        void shouldCreateOAuth2AuthorizedClientService() {
            // Given
            var databaseClient = mock(org.springframework.r2dbc.core.DatabaseClient.class);
            ReactiveClientRegistrationRepository clientRepository = mock(ReactiveClientRegistrationRepository.class);

            // When
            ReactiveOAuth2AuthorizedClientService clientService = 
                securityConfiguration.oAuth2ClientService(databaseClient, clientRepository);

            // Then
            assertThat(clientService).isNotNull();
            assertThat(clientService).isInstanceOf(org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService.class);
        }
    }
}
package com.plate.boot.security.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.server.WebSession;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuthenticationToken Unit Tests
 *
 * <p>This test class provides unit tests for the AuthenticationToken record, covering:</p>
 * <ul>
 *   <li>Creation of AuthenticationToken instances</li>
 *   <li>Building AuthenticationToken from session and authentication</li>
 *   <li>Immutability and data integrity</li>
 * </ul>
 *
 * @author Qwen Code
 */
class AuthenticationTokenTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create AuthenticationToken with of method")
        void shouldCreateAuthenticationTokenWithOfMethod() {
            // Given
            String token = "test-token";
            Long expires = 1234567890L;
            Long lastAccessTime = 1234567800L;
            String details = "test-details";

            // When
            AuthenticationToken authenticationToken = AuthenticationToken.of(token, expires, lastAccessTime, details);

            // Then
            assertThat(authenticationToken).isNotNull();
            assertThat(authenticationToken.token()).isEqualTo(token);
            assertThat(authenticationToken.expires()).isEqualTo(expires);
            assertThat(authenticationToken.lastAccessTime()).isEqualTo(lastAccessTime);
            assertThat(authenticationToken.details()).isEqualTo(details);
        }

        @Test
        @DisplayName("Should create AuthenticationToken with constructor")
        void shouldCreateAuthenticationTokenWithConstructor() {
            // Given
            String token = "test-token";
            Long expires = 1234567890L;
            Long lastAccessTime = 1234567800L;
            String details = "test-details";

            // When
            AuthenticationToken authenticationToken = new AuthenticationToken(token, expires, lastAccessTime, details);

            // Then
            assertThat(authenticationToken).isNotNull();
            assertThat(authenticationToken.token()).isEqualTo(token);
            assertThat(authenticationToken.expires()).isEqualTo(expires);
            assertThat(authenticationToken.lastAccessTime()).isEqualTo(lastAccessTime);
            assertThat(authenticationToken.details()).isEqualTo(details);
        }
    }

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("Should build AuthenticationToken from session and authentication")
        void shouldBuildAuthenticationTokenFromSessionAndAuthentication() {
            // Given
            WebSession session = mock(WebSession.class);
            when(session.getId()).thenReturn("test-session-id");
            when(session.getMaxIdleTime()).thenReturn(java.time.Duration.ofHours(1));
            when(session.getLastAccessTime()).thenReturn(Instant.ofEpochSecond(1234567800L));
            
            User user = new User("testuser", "encodedPassword", 
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(user, "credentials", user.getAuthorities());

            // When
            AuthenticationToken authenticationToken = AuthenticationToken.build(session, authentication);

            // Then
            assertThat(authenticationToken).isNotNull();
            assertThat(authenticationToken.token()).isEqualTo("test-session-id");
            assertThat(authenticationToken.expires()).isEqualTo(3600L); // 1 hour in seconds
            assertThat(authenticationToken.lastAccessTime()).isEqualTo(1234567800L);
            assertThat(authenticationToken.details()).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should maintain immutability")
        void shouldMaintainImmutability() {
            // Given
            String token = "test-token";
            Long expires = 1234567890L;
            Long lastAccessTime = 1234567800L;
            String details = "test-details";
            AuthenticationToken authenticationToken = AuthenticationToken.of(token, expires, lastAccessTime, details);

            // When - Try to modify the values (which should have no effect due to immutability)
            // We can't actually modify the record fields since they're final, but we can verify they're unchanged

            // Then
            assertThat(authenticationToken.token()).isEqualTo(token);
            assertThat(authenticationToken.expires()).isEqualTo(expires);
            assertThat(authenticationToken.lastAccessTime()).isEqualTo(lastAccessTime);
            assertThat(authenticationToken.details()).isEqualTo(details);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() {
            // Given
            String token = "test-token";
            Long expires = 1234567890L;
            Long lastAccessTime = 1234567800L;
            String details = "test-details";
            AuthenticationToken authenticationToken = AuthenticationToken.of(token, expires, lastAccessTime, details);

            // Then - Just verify it's an instance of Serializable
            assertThat(authenticationToken).isInstanceOf(java.io.Serializable.class);
        }
    }
}
package com.plate.boot.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthenticationToken}, focusing on the {@code build(WebSession, Object)}
 * factory which must tolerate nullable session timestamps (last access time / max idle time)
 * instead of throwing a {@link NullPointerException}.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationTokenTest {

    @Mock
    private WebSession session;

    @Test
    void buildShouldNotThrowWhenSessionTimestampsAreNull() {
        when(session.getId()).thenReturn("session-123");
        when(session.getLastAccessTime()).thenReturn(null);
        when(session.getMaxIdleTime()).thenReturn(null);

        AuthenticationToken token = AuthenticationToken.build(session, "principal");

        assertThat(token).isNotNull();
        assertThat(token.token()).isEqualTo("session-123");
        assertThat(token.lastAccessTime()).isNotNull();
        assertThat(token.expires()).isEqualTo(1800L);
        assertThat(token.details()).isEqualTo("principal");
    }

    @Test
    void buildShouldUseProvidedTimestampsWhenPresent() {
        Instant lastAccess = Instant.parse("2026-01-01T00:00:00Z");
        when(session.getId()).thenReturn("session-456");
        when(session.getLastAccessTime()).thenReturn(lastAccess);
        when(session.getMaxIdleTime()).thenReturn(Duration.ofSeconds(600));

        AuthenticationToken token = AuthenticationToken.build(session, "principal");

        assertThat(token.token()).isEqualTo("session-456");
        assertThat(token.lastAccessTime()).isEqualTo(lastAccess.getEpochSecond());
        assertThat(token.expires()).isEqualTo(600L);
    }
}

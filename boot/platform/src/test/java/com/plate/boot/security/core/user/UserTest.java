package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.UserAuditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link User} (no Spring / container required).
 */
class UserTest {

    private static JsonMapper savedMapper;

    @BeforeAll
    static void setUpMapper() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDownMapper() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @Test
    void accessorsRoundTrip() {
        User user = new User();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime loginTime = LocalDateTime.now().minusHours(1);

        user.setId(1L);
        user.setVersion(1L);
        user.setCode(code);
        user.setTenantCode(tenantCode);
        user.setExtend(extend);
        user.setCreatedBy(createdBy);
        user.setCreatedAt(createdAt);
        user.setUpdatedBy(createdBy);
        user.setUpdatedAt(createdAt);
        user.setQuery(Map.of("q", "v"));
        user.setSearch("search");
        user.setSecurityCode(UUID.randomUUID());
        user.setUsername("alice");
        user.setPassword("Password1");
        user.setDisabled(false);
        user.setAccountExpired(false);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
        user.setEmail("alice@example.com");
        user.setPhone("13812345678");
        user.setName("Alice");
        user.setAvatar("avatar.png");
        user.setBio("bio");
        user.setLoginTime(loginTime);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getVersion()).isEqualTo(1L);
        assertThat(user.getCode()).isEqualTo(code);
        assertThat(user.getTenantCode()).isEqualTo(tenantCode);
        assertThat(user.getExtend()).isEqualTo(extend);
        assertThat(user.getCreatedBy()).isEqualTo(createdBy);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedBy()).isEqualTo(createdBy);
        assertThat(user.getUpdatedAt()).isEqualTo(createdAt);
        assertThat(user.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(user.getSearch()).isEqualTo("search");
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).isEqualTo("Password1");
        assertThat(user.getDisabled()).isFalse();
        assertThat(user.getAccountExpired()).isFalse();
        assertThat(user.getAccountLocked()).isFalse();
        assertThat(user.getCredentialsExpired()).isFalse();
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPhone()).isEqualTo("13812345678");
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getAvatar()).isEqualTo("avatar.png");
        assertThat(user.getBio()).isEqualTo("bio");
        assertThat(user.getLoginTime()).isEqualTo(loginTime);
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        User a = new User();
        a.setId(1L);
        a.setUsername("alice");

        User b = new User();
        b.setId(1L);
        b.setUsername("alice");

        User c = new User();
        c.setId(2L);
        c.setUsername("alice");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        User user = new User();
        user.setUsername("alice");
        assertThat(user.toString()).contains("alice");
    }

    @Test
    void isNewBehaviour() {
        User user = new User();
        assertThat(user.isNew()).isTrue();
        assertThat(user.getCode()).isNotNull();

        User persisted = new User();
        persisted.setId(3L);
        assertThat(persisted.isNew()).isFalse();
    }
}

package com.plate.boot.relational.logger;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.relational.MethodType;
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
 * Unit tests for {@link Logger} (no Spring / container required).
 */
class LoggerTest {

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
        Logger logger = new Logger();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID securityCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        JsonNode context = ContextUtils.OBJECT_MAPPER.createObjectNode().put("k", "v");
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        UserAuditor updatedBy = UserAuditor.of(UUID.randomUUID(), "updater");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        logger.setId(1L);
        logger.setVersion(1L);
        logger.setCode(code);
        logger.setTenantCode(tenantCode);
        logger.setExtend(extend);
        logger.setCreatedBy(createdBy);
        logger.setCreatedAt(createdAt);
        logger.setUpdatedBy(updatedBy);
        logger.setUpdatedAt(updatedAt);
        logger.setQuery(Map.of("q", "v"));
        logger.setSearch("search");
        logger.setSecurityCode(securityCode);
        logger.setOperator("admin");
        logger.setPrefix("SEC");
        logger.setUrl("/sec/users");
        logger.setMethod(MethodType.POST);
        logger.setStatus("success");
        logger.setContext(context);

        assertThat(logger.getId()).isEqualTo(1L);
        assertThat(logger.getVersion()).isEqualTo(1L);
        assertThat(logger.getCode()).isEqualTo(code);
        assertThat(logger.getTenantCode()).isEqualTo(tenantCode);
        assertThat(logger.getExtend()).isEqualTo(extend);
        assertThat(logger.getCreatedBy()).isEqualTo(createdBy);
        assertThat(logger.getCreatedAt()).isEqualTo(createdAt);
        assertThat(logger.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(logger.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(logger.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(logger.getSearch()).isEqualTo("search");
        assertThat(logger.getSecurityCode()).isEqualTo(securityCode);
        assertThat(logger.getOperator()).isEqualTo("admin");
        assertThat(logger.getPrefix()).isEqualTo("SEC");
        assertThat(logger.getUrl()).isEqualTo("/sec/users");
        assertThat(logger.getMethod()).isEqualTo(MethodType.POST);
        assertThat(logger.getStatus()).isEqualTo("success");
        assertThat(logger.getContext()).isEqualTo(context);
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        Logger a = new Logger();
        a.setId(5L);
        a.setOperator("admin");
        a.setUrl("/x");

        Logger b = new Logger();
        b.setId(5L);
        b.setOperator("admin");
        b.setUrl("/x");

        Logger c = new Logger();
        c.setId(6L);
        c.setOperator("admin");
        c.setUrl("/x");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        Logger logger = new Logger();
        logger.setOperator("admin");
        logger.setStatus("success");
        assertThat(logger.toString()).contains("admin", "success");
    }

    @Test
    void isNewBehaviour() {
        Logger logger = new Logger();
        assertThat(logger.isNew()).isTrue();
        assertThat(logger.getCode()).isNotNull();

        Logger persisted = new Logger();
        persisted.setId(4L);
        assertThat(persisted.isNew()).isFalse();
    }
}

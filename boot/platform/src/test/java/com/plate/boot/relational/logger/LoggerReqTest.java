package com.plate.boot.relational.logger;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.relational.MethodType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link LoggerReq} (no Spring / container required).
 * Also exercises {@link MethodType#value(String)} / {@link MethodType#resolve(String)}
 * because {@link LoggerReq#of} relies on them.
 */
class LoggerReqTest {

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
    void ofBuildsRequestFromParameters() {
        UUID tenantCode = UUID.randomUUID();
        JsonNode context = ContextUtils.OBJECT_MAPPER.createObjectNode().put("path", "/x");

        LoggerReq req = LoggerReq.of(tenantCode, "admin", "SEC", "POST", "success", "/sec/users", context);

        assertThat(req.getTenantCode()).isEqualTo(tenantCode);
        assertThat(req.getOperator()).isEqualTo("admin");
        assertThat(req.getPrefix()).isEqualTo("SEC");
        assertThat(req.getUrl()).isEqualTo("/sec/users");
        assertThat(req.getMethod()).isEqualTo(MethodType.POST);
        assertThat(req.getStatus()).isEqualTo("success");
        assertThat(req.getContext()).isEqualTo(context);
    }

    @Test
    void toLoggerCopiesProperties() {
        LoggerReq req = new LoggerReq();
        req.setOperator("user");
        req.setStatus("error");
        req.setUrl("/rel/menus");
        req.setMethod(MethodType.DELETE);

        Logger logger = req.toLogger();

        assertThat(logger.getOperator()).isEqualTo("user");
        assertThat(logger.getStatus()).isEqualTo("error");
        assertThat(logger.getUrl()).isEqualTo("/rel/menus");
        assertThat(logger.getMethod()).isEqualTo(MethodType.DELETE);
    }

    @Test
    void methodTypeValueResolvesKnownMethods() {
        assertThat(MethodType.value("POST")).isEqualTo(MethodType.POST);
        assertThat(MethodType.value("PUT")).isEqualTo(MethodType.PUT);
        assertThat(MethodType.value("DELETE")).isEqualTo(MethodType.DELETE);
        assertThat(MethodType.value("UNKNOWN")).isEqualTo(MethodType.UNKNOWN);
    }

    @Test
    void methodTypeResolveReturnsNullForUnknown() {
        assertThat(MethodType.resolve("PATCH")).isNull();
    }

    @Test
    void methodTypeValueThrowsForUnknown() {
        assertThatThrownBy(() -> MethodType.value("PATCH"))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void equalsHashCodeAndToString() {
        LoggerReq a = new LoggerReq();
        a.setId(1L);
        LoggerReq b = new LoggerReq();
        b.setId(1L);
        LoggerReq c = new LoggerReq();
        c.setId(2L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("LoggerReq");
    }
}

package com.plate.boot.relational.dictionaries;

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
 * Unit tests for {@link Dictionary} (no Spring / container required).
 */
class DictionaryTest {

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
        Dictionary dictionary = new Dictionary();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID pcode = UUID.randomUUID();
        UUID securityCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        UserAuditor updatedBy = UserAuditor.of(UUID.randomUUID(), "updater");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        dictionary.setId(100L);
        dictionary.setVersion(3L);
        dictionary.setCode(code);
        dictionary.setTenantCode(tenantCode);
        dictionary.setExtend(extend);
        dictionary.setCreatedBy(createdBy);
        dictionary.setCreatedAt(createdAt);
        dictionary.setUpdatedBy(updatedBy);
        dictionary.setUpdatedAt(updatedAt);
        dictionary.setQuery(Map.of("q", "v"));
        dictionary.setSearch("keyword");
        dictionary.setSecurityCode(securityCode);
        dictionary.setPcode(pcode);
        dictionary.setDictType("USER_STATUS");
        dictionary.setDictKey("ACTIVE");
        dictionary.setDictValue("1");
        dictionary.setDictLabel("Active");
        dictionary.setDescription("An active user");
        dictionary.setSortNo(5);
        dictionary.setEnabled(true);

        assertThat(dictionary.getId()).isEqualTo(100L);
        assertThat(dictionary.getVersion()).isEqualTo(3L);
        assertThat(dictionary.getCode()).isEqualTo(code);
        assertThat(dictionary.getTenantCode()).isEqualTo(tenantCode);
        assertThat(dictionary.getExtend()).isEqualTo(extend);
        assertThat(dictionary.getCreatedBy()).isEqualTo(createdBy);
        assertThat(dictionary.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dictionary.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(dictionary.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(dictionary.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(dictionary.getSearch()).isEqualTo("keyword");
        assertThat(dictionary.getSecurityCode()).isEqualTo(securityCode);
        assertThat(dictionary.getPcode()).isEqualTo(pcode);
        assertThat(dictionary.getDictType()).isEqualTo("USER_STATUS");
        assertThat(dictionary.getDictKey()).isEqualTo("ACTIVE");
        assertThat(dictionary.getDictValue()).isEqualTo("1");
        assertThat(dictionary.getDictLabel()).isEqualTo("Active");
        assertThat(dictionary.getDescription()).isEqualTo("An active user");
        assertThat(dictionary.getSortNo()).isEqualTo(5);
        assertThat(dictionary.getEnabled()).isTrue();
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        Dictionary a = new Dictionary();
        a.setId(1L);
        a.setDictType("GENDER");
        a.setDictKey("M");

        Dictionary b = new Dictionary();
        b.setId(1L);
        b.setDictType("GENDER");
        b.setDictKey("M");

        Dictionary c = new Dictionary();
        c.setId(2L);
        c.setDictType("GENDER");
        c.setDictKey("M");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        Dictionary dictionary = new Dictionary();
        dictionary.setDictType("COUNTRY");
        dictionary.setDictKey("CN");
        assertThat(dictionary.toString()).contains("COUNTRY", "CN");
    }

    @Test
    void isNewAssignsCodeAndReturnsTrue() {
        Dictionary dictionary = new Dictionary();
        assertThat(dictionary.isNew()).isTrue();
        assertThat(dictionary.getCode()).isNotNull();
    }

    @Test
    void isNewReturnsFalseWhenIdPresent() {
        Dictionary dictionary = new Dictionary();
        dictionary.setId(9L);
        assertThat(dictionary.isNew()).isFalse();
    }
}

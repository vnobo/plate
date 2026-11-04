package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GroupMemberRes} (no Spring / container required).
 */
class GroupMemberResTest {

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
        GroupMemberRes res = new GroupMemberRes();
        JsonNode groupExtend = ContextUtils.OBJECT_MAPPER.createObjectNode().put("k", "v");

        res.setName("Alice");
        res.setGroupExtend(groupExtend);

        assertThat(res.getName()).isEqualTo("Alice");
        assertThat(res.getGroupExtend()).isEqualTo(groupExtend);
    }

    @Test
    void equalsAndHashCodeIncludeFields() {
        GroupMemberRes a = new GroupMemberRes();
        a.setId(1L);
        a.setName("Alice");

        GroupMemberRes b = new GroupMemberRes();
        b.setId(1L);
        b.setName("Alice");

        GroupMemberRes c = new GroupMemberRes();
        c.setId(1L);
        c.setName("Bob");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        GroupMemberRes res = new GroupMemberRes();
        res.setName("Alice");
        assertThat(res.toString()).contains("Alice");
    }
}

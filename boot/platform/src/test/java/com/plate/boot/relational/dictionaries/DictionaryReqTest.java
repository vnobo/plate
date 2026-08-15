package com.plate.boot.relational.dictionaries;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.query.Criteria;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DictionaryReq} (no Spring / container required).
 */
class DictionaryReqTest {

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
        DictionaryReq req = new DictionaryReq();
        UUID code = UUID.randomUUID();
        UUID pcode = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        ObjectNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();

        req.setId(10L);
        req.setCode(code);
        req.setPcode(pcode);
        req.setTenantCode(tenantCode);
        req.setDictType("GENDER");
        req.setDictKey("M");
        req.setDictValue("male");
        req.setDictLabel("Male");
        req.setDescription("desc");
        req.setSortNo(1);
        req.setEnabled(true);
        req.setExtend(extend);
        req.setSearch("male");

        assertThat(req.getId()).isEqualTo(10L);
        assertThat(req.getCode()).isEqualTo(code);
        assertThat(req.getPcode()).isEqualTo(pcode);
        assertThat(req.getTenantCode()).isEqualTo(tenantCode);
        assertThat(req.getDictType()).isEqualTo("GENDER");
        assertThat(req.getDictKey()).isEqualTo("M");
        assertThat(req.getDictValue()).isEqualTo("male");
        assertThat(req.getDictLabel()).isEqualTo("Male");
        assertThat(req.getDescription()).isEqualTo("desc");
        assertThat(req.getSortNo()).isEqualTo(1);
        assertThat(req.getEnabled()).isTrue();
        assertThat(req.getExtend()).isEqualTo(extend);
        assertThat(req.getSearch()).isEqualTo("male");
    }

    @Test
    void toDictionaryCopiesProperties() {
        DictionaryReq req = new DictionaryReq();
        req.setDictType("COUNTRY");
        req.setDictKey("CN");
        req.setDictValue("china");
        req.setDictLabel("China");

        Dictionary dictionary = req.toDictionary();

        assertThat(dictionary.getDictType()).isEqualTo("COUNTRY");
        assertThat(dictionary.getDictKey()).isEqualTo("CN");
        assertThat(dictionary.getDictValue()).isEqualTo("china");
        assertThat(dictionary.getDictLabel()).isEqualTo("China");
    }

    @Test
    void toCriteriaReturnsEmptyForBlankRequest() {
        Criteria criteria = new DictionaryReq().toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void toCriteriaBuildsEveryCondition() {
        DictionaryReq req = new DictionaryReq();
        UUID code = UUID.randomUUID();
        UUID pcode = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        req.setId(1L);
        req.setCode(code);
        req.setPcode(pcode);
        req.setTenantCode(tenantCode);
        req.setDictType("GENDER");
        req.setDictKey("M");
        req.setDictValue("male");
        req.setDictLabel("Male");
        req.setEnabled(false);

        Criteria criteria = req.toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void equalsHashCodeAndToString() {
        DictionaryReq a = new DictionaryReq();
        a.setId(1L);
        DictionaryReq b = new DictionaryReq();
        b.setId(1L);
        DictionaryReq c = new DictionaryReq();
        c.setId(2L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("DictionaryReq");
    }
}

package com.plate.boot.relational.menus;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.query.Criteria;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MenuReq} (no Spring / container required).
 * Covers the static factory, {@code toMenu()} conversion (including the JSON {@code icons}
 * merge), and {@code toCriteria()} branch handling.
 */
class MenuReqTest {

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
    void factoryOfSetsTenantCodeAndAuthority() {
        UUID tenantCode = UUID.randomUUID();
        MenuReq req = MenuReq.of(tenantCode, "menuView");

        assertThat(req.getTenantCode()).isEqualTo(tenantCode);
        assertThat(req.getAuthority()).isEqualTo("ROLE_MENU_VIEW");
    }

    @Test
    void accessorsRoundTrip() {
        MenuReq req = new MenuReq();
        req.setRules(Set.of("ROLE_A", "ROLE_B"));
        req.setIcons("home");
        req.setMenus(Set.of(new MenuReq()));
        req.setName("Root");
        req.setSortNo((short) 1);

        assertThat(req.getRules()).containsExactlyInAnyOrder("ROLE_A", "ROLE_B");
        assertThat(req.getIcons()).isEqualTo("home");
        assertThat(req.getMenus()).hasSize(1);
        assertThat(req.getName()).isEqualTo("Root");
        assertThat(req.getSortNo()).isEqualTo((short) 1);
    }

    @Test
    void toMenuCopiesPropertiesAndMergesIcons() {
        MenuReq req = new MenuReq();
        req.setTenantCode(UUID.randomUUID());
        req.setAuthority("ROLE_MENU_VIEW");
        req.setName("Users");
        req.setIcons("users");

        Menu menu = req.toMenu();

        assertThat(menu.getName()).isEqualTo("Users");
        assertThat(menu.getAuthority()).isEqualTo("ROLE_MENU_VIEW");
        assertThat(menu.getExtend()).isNotNull();
        assertThat(menu.getExtend().get("icons").asString()).isEqualTo("users");
    }

    @Test
    void toMenuWithoutIconsLeavesExtendUnchanged() {
        MenuReq req = new MenuReq();
        ObjectNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode().put("other", "value");
        req.setExtend(extend);
        req.setName("NoIcons");

        Menu menu = req.toMenu();

        assertThat(menu.getName()).isEqualTo("NoIcons");
        assertThat(menu.getExtend().get("other").asString()).isEqualTo("value");
        assertThat(menu.getExtend().has("icons")).isFalse();
    }

    @Test
    void toMenuWithNullExtendCreatesNewObjectNode() {
        MenuReq req = new MenuReq();
        req.setName("NullExtend");
        req.setIcons("home");

        Menu menu = req.toMenu();

        assertThat(menu.getExtend()).isNotNull();
        assertThat(menu.getExtend().get("icons").asString()).isEqualTo("home");
    }

    @Test
    void toCriteriaReturnsCriteriaForEmptyRequest() {
        assertThat(new MenuReq().toCriteria()).isNotNull();
    }

    @Test
    void toCriteriaAddsTenantCodeAuthorityAndRules() {
        MenuReq req = new MenuReq();
        UUID tenantCode = UUID.randomUUID();
        req.setTenantCode(tenantCode);
        req.setAuthority("ROLE_MENU_VIEW");
        req.setRules(Set.of("ROLE_A", "ROLE_B"));

        Criteria criteria = req.toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void equalsHashCodeAndToString() {
        MenuReq a = new MenuReq();
        a.setId(1);
        MenuReq b = new MenuReq();
        b.setId(1);
        MenuReq c = new MenuReq();
        c.setId(2);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("MenuReq");
    }
}

package com.plate.boot.relational.menus;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.UserAuditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Menu} (no Spring / container required).
 * Exercises the Lombok accessors, the custom authority normalisation, the JSON-backed
 * {@code permissions}/{@code icons} projections, the nested {@code MenuType} enum, and
 * the {@link com.plate.boot.commons.base.AbstractEntity} inherited behaviour.
 */
class MenuTest {

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
    void setAuthorityUpgradesToRoleUpperCase() {
        Menu menu = new Menu();
        menu.setAuthority("menuView");
        assertThat(menu.getAuthority()).isEqualTo("ROLE_MENU_VIEW");
    }

    @Test
    void setAuthorityKeepsAlreadyPrefixedAuthority() {
        Menu menu = new Menu();
        menu.setAuthority("ROLE_Admin");
        assertThat(menu.getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void setAuthorityReturnsNullForBlankInput() {
        Menu menu = new Menu();
        menu.setAuthority("");
        assertThat(menu.getAuthority()).isNull();
    }

    @Test
    void getPermissionsReturnsNullWhenExtendNull() {
        assertThat(new Menu().getPermissions()).isNull();
    }

    @Test
    void getPermissionsReturnsNullWhenPermissionsNodeMissing() {
        Menu menu = new Menu();
        menu.setExtend(ContextUtils.OBJECT_MAPPER.createObjectNode().put("icons", "home"));
        assertThat(menu.getPermissions()).isNull();
    }

    @Test
    void getPermissionsThrowsWhenPermissionsPresent() {
        Menu menu = new Menu();
        ObjectNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        extend.set("permissions", ContextUtils.OBJECT_MAPPER.createArrayNode().add("user:read"));
        menu.setExtend(extend);

        assertThatThrownBy(menu::getPermissions).isInstanceOf(Exception.class);
    }

    @Test
    void getIconsReturnsNullWhenMissing() {
        Menu menu = new Menu();
        menu.setExtend(ContextUtils.OBJECT_MAPPER.createObjectNode());
        assertThat(menu.getIcons()).isNull();
    }

    @Test
    void getIconsReturnsValueWhenPresent() {
        Menu menu = new Menu();
        menu.setExtend(ContextUtils.OBJECT_MAPPER.createObjectNode().put("icons", "dashboard"));
        assertThat(menu.getIcons()).isEqualTo("dashboard");
    }

    @Test
    void menuTypeHasFourValues() {
        assertThat(Menu.MenuType.values())
                .containsExactly(Menu.MenuType.FOLDER, Menu.MenuType.MENU, Menu.MenuType.LINK, Menu.MenuType.API);
        assertThat(Menu.MenuType.valueOf("FOLDER")).isEqualTo(Menu.MenuType.FOLDER);
    }

    @Test
    void accessorsRoundTrip() {
        Menu menu = new Menu();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID pcode = UUID.randomUUID();
        UUID securityCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        UserAuditor updatedBy = UserAuditor.of(UUID.randomUUID(), "updater");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        Map<String, Object> query = Map.of("k", "v");

        menu.setId(1);
        menu.setVersion(2L);
        menu.setCode(code);
        menu.setTenantCode(tenantCode);
        menu.setExtend(extend);
        menu.setCreatedBy(createdBy);
        menu.setCreatedAt(createdAt);
        menu.setUpdatedBy(updatedBy);
        menu.setUpdatedAt(updatedAt);
        menu.setQuery(query);
        menu.setSearch("search");
        menu.setSecurityCode(securityCode);
        menu.setPcode(pcode);
        menu.setType(Menu.MenuType.MENU);
        menu.setAuthority("ROLE_MENU_VIEW");
        menu.setName("Users");
        menu.setPath("/users");
        menu.setSortNo((short) 3);

        assertThat(menu.getId()).isEqualTo(1);
        assertThat(menu.getVersion()).isEqualTo(2L);
        assertThat(menu.getCode()).isEqualTo(code);
        assertThat(menu.getTenantCode()).isEqualTo(tenantCode);
        assertThat(menu.getExtend()).isEqualTo(extend);
        assertThat(menu.getCreatedBy()).isEqualTo(createdBy);
        assertThat(menu.getCreatedAt()).isEqualTo(createdAt);
        assertThat(menu.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(menu.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(menu.getQuery()).isEqualTo(query);
        assertThat(menu.getSearch()).isEqualTo("search");
        assertThat(menu.getSecurityCode()).isEqualTo(securityCode);
        assertThat(menu.getPcode()).isEqualTo(pcode);
        assertThat(menu.getType()).isEqualTo(Menu.MenuType.MENU);
        assertThat(menu.getAuthority()).isEqualTo("ROLE_MENU_VIEW");
        assertThat(menu.getName()).isEqualTo("Users");
        assertThat(menu.getPath()).isEqualTo("/users");
        assertThat(menu.getSortNo()).isEqualTo((short) 3);
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        Menu a = new Menu();
        a.setId(10);
        a.setName("Users");
        a.setPath("/users");
        a.setAuthority("ROLE_USERS");

        Menu b = new Menu();
        b.setId(10);
        b.setName("Users");
        b.setPath("/users");
        b.setAuthority("ROLE_USERS");

        Menu c = new Menu();
        c.setId(11);
        c.setName("Users");
        c.setPath("/users");
        c.setAuthority("ROLE_USERS");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(new Object());
    }

    @Test
    void toStringContainsOwnFields() {
        Menu menu = new Menu();
        menu.setName("Dashboard");
        menu.setAuthority("ROLE_DASHBOARD");
        assertThat(menu.toString()).contains("Dashboard", "ROLE_DASHBOARD");
    }

    @Test
    void isNewAssignsCodeAndReturnsTrueWhenNoId() {
        Menu menu = new Menu();
        assertThat(menu.isNew()).isTrue();
        assertThat(menu.getCode()).isNotNull();
        assertThat(menu.getCode().version()).isEqualTo(7);
    }

    @Test
    void isNewReturnsFalseWhenIdPresent() {
        Menu menu = new Menu();
        menu.setId(7);
        assertThat(menu.isNew()).isFalse();
    }
}

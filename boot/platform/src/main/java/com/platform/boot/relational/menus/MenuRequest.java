package com.platform.boot.relational.menus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * @author Alex bob
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuRequest extends Menu {

    private Set<String> rules;

    private String icons;

    @Valid
    private Set<MenuRequest> menus;

    @Valid
    private Set<Permission> permissions;

    public static MenuRequest of(String tenantCode, String authority) {
        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setTenantCode(tenantCode);
        menuRequest.setAuthority(authority);
        return menuRequest;
    }

    public Menu toMenu() {
        Menu menu = BeanUtils.copyProperties(this, Menu.class);
        ObjectNode objectNode = Optional.ofNullable(this.getExtend())
                .map(node -> (ObjectNode) node.deepCopy())
                .orElse(ContextUtils.OBJECT_MAPPER.createObjectNode());
        if (!ObjectUtils.isEmpty(this.permissions)) {
            objectNode.putPOJO("permissions", this.permissions);
        }
        if (StringUtils.hasLength(this.icons)) {
            objectNode.put("icons", this.icons);
        }
        menu.setExtend(objectNode);
        return menu;
    }

    public Criteria toCriteria() {
        Criteria criteria = criteria(Set.of("permissions", "tenantCode", "icons", "menus", "rules"));

        Criteria tenantCriteria = Criteria.where("tenantCode").is("0");
        if (StringUtils.hasLength(this.getTenantCode())) {
            tenantCriteria = tenantCriteria.or("tenantCode").is(this.getTenantCode());
        }
        criteria = criteria.and(tenantCriteria);

        if (StringUtils.hasLength(this.getAuthority())) {
            criteria = criteria.and("authority").is(this.getAuthority());
        }

        if (!ObjectUtils.isEmpty(this.getRules())) {
            criteria = criteria.and("authority").in(this.getRules());
        }

        return criteria;
    }

}
package com.plate.boot.relational.menus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuRequest extends Menu {

    private Set<String> rules;

    private String icons;

    @Valid
    private Set<MenuRequest> menus;

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
        if (StringUtils.hasLength(this.icons)) {
            objectNode.put("icons", this.icons);
        }
        menu.setExtend(objectNode);
        return menu;
    }

    public Criteria toCriteria() {
        Criteria criteria = criteria(Set.of("permissions", "tenantCode", "icons", "menus", "rules"));

        if (StringUtils.hasLength(this.getTenantCode())) {
            criteria = criteria.and("tenantCode").in(List.of(this.getTenantCode(), "0"));
        }

        if (StringUtils.hasLength(this.getAuthority())) {
            criteria = criteria.and("authority").is(this.getAuthority());
        }

        if (!ObjectUtils.isEmpty(this.getRules())) {
            criteria = criteria.and("authority").in(this.getRules());
        }

        return criteria;
    }

}
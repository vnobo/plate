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
import java.util.UUID;

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;

/**
 * MenuReq class extends Menu and provides additional properties and methods
 * for handling menu requests.
 *
 * <p>This class includes properties for rules, icons, and nested menus, and
 * provides methods to convert to a Menu object and to create query criteria.
 * </p>
 *
 * <p>Author: <a href="https://github.com/vnobo">Alex Bob</a></p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuReq extends Menu {

    /**
     * Set of rules associated with the menu request.
     */
    private Set<String> rules;

    /**
     * Icons associated with the menu request.
     */
    private String icons;

    /**
     * Set of nested MenuReq objects.
     */
    @Valid
    private Set<MenuReq> menus;

    /**
     * Creates a new MenuReq object with the specified tenant code and authority.
     *
     * @param tenantCode the tenant code
     * @param authority  the authority
     * @return a new MenuReq object
     */
    public static MenuReq of(UUID tenantCode, String authority) {
        MenuReq menuReq = new MenuReq();
        menuReq.setTenantCode(tenantCode);
        menuReq.setAuthority(authority);
        return menuReq;
    }

    /**
     * Converts this MenuReq object to a Menu object.
     *
     * @return a Menu object
     */
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

    /**
     * Creates a Criteria object based on the properties of this MenuReq object.
     *
     * @return a Criteria object
     */
    public Criteria toCriteria() {
        Criteria criteria = criteria(Set.of("permissions", "tenantCode", "icons", "menus", "rules"));

        if (!ObjectUtils.isEmpty(this.getTenantCode())) {
            criteria = criteria.and("tenantCode").in(List.of(this.getTenantCode(), DEFAULT_UUID_CODE));
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
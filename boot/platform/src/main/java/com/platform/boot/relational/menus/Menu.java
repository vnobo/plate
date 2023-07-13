package com.platform.boot.relational.menus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.CaseFormat;
import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.commons.utils.ContextHolder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * A representation of a menu in the application.
 * Menus are stored in the "se_menus" table in the database.
 * This class implements the BaseEntity interface and has an Integer id field.
 * BaseEntity <Integer> the type of the id field
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@Table("se_menus")
public class Menu implements BaseEntity<Integer> {

    /**
     * The unique identifier of this menu.
     */
    @Id
    private Integer id;

    /**
     * The code for this menu.
     */
    private String code;

    /**
     * The parent code of this menu.
     * The parent code can be blank if this menu is a root menu.
     */
    @NotBlank(message = "Parent code cannot be blank!")
    private String pcode;

    /**
     * The tenant code of this menu.
     * The tenant code cannot be blank and identifies the tenant to which this menu belongs.
     */
    @NotBlank(message = "Tenant code cannot be blank!")
    private String tenantCode;

    /**
     * The type of this menu.
     * The type cannot be null and must be one of the values from the MenuType enum.
     */
    @NotNull(message = "Menu type cannot be null!")
    private MenuType type;

    /**
     * The authority of this menu.
     * The authority cannot be blank and must contain only English letters or '_' symbols.
     * For example: user, user_add, user_delete.
     */
    @NotBlank(message = "Authority cannot be blank!")
    @Pattern(regexp = "^[a-zA-Z_]{1,50}$", message = "Authority can only contain English letters or '_' symbols.")
    private String authority;

    /**
     * The name of this menu.
     * The name cannot be blank and is used to display the menu in the application.
     */
    @NotBlank(message = "Name cannot be blank!")
    private String name;

    /**
     * The path of this menu.
     * The path can be blank if this menu does not have a path.
     */
    private String path;

    /**
     * The sort  of this menu.
     * The sort  can be null and is used to sort menus in the application.
     * A lower sort  means the menu will be displayed before menus with a higher sort index.
     */
    private Short sort;

    /**
     * Additional data for this menu.
     * The extend field can be null and is used to store additional data for this menu in JSON format.
     */
    private JsonNode extend;

    /**
     * The date and time that this menu was created.
     * The createdTime field cannot be null and is set automatically by the database.
     */
    @CreatedDate
    private LocalDateTime createdTime;

    /**
     * The date and time that this menu was last modified.
     * The updatedTime field cannot be null and is set automatically by the database.
     */
    @LastModifiedDate
    private LocalDateTime updatedTime;

    /**
     * Sets the authority for this permission, upgrading to uppercase and adding prefix if necessary.
     *
     * @param authority the original authority to set for this permission
     */
    public void setAuthority(String authority) {
        this.authority = upgradeAuthorityUpperCase(authority);
    }

    /**
     * Upgrades the authority string to uppercase and adds prefix if necessary.
     *
     * @param authority original authority string
     * @return upgraded and prefixed authority string in uppercase
     */
    private String upgradeAuthorityUpperCase(String authority) {
        if (!StringUtils.hasLength(authority)) {
            return null;
        }
        String role = authority;
        if (!authority.startsWith(MenusService.AUTHORITY_PREFIX)) {
            role = MenusService.AUTHORITY_PREFIX + "_"
                    + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, authority);
        }
        role = role.toUpperCase();
        return role;
    }

    /**
     * Retrieves the permissions associated with this object.
     *
     * @return a set of permissions associated with this object, or null if none exist
     */
    @JsonGetter
    public Set<Permission> getPermissions() {
        return Optional.ofNullable(this.getExtend()).map(node -> node.get("permissions"))
                .map(node -> ContextHolder.OBJECT_MAPPER.convertValue(node, new TypeReference<Set<Permission>>() {
                })).orElse(null);
    }

    /**
     * Retrieves the icon associated with this object.
     *
     * @return the icon associated with this object, or null if none exists
     */
    @JsonGetter
    public String getIcons() {
        return Optional.ofNullable(this.getExtend()).map(node -> node.get("icons"))
                .map(JsonNode::asText).orElse(null);
    }

    enum MenuType {
        FOLDER,
        MENU
    }

    enum HttpMethod {
        /**
         * GET 查询
         */
        GET,
        /**
         * POST 增加
         */
        POST,
        /**
         * PUT 修改
         */
        PUT,
        /**
         * DELETE 删除
         */
        DELETE,
        /**
         * 包含所有
         */
        ALL
    }

    /**
     * Permission class represents permissions for a certain API endpoint.
     * It includes the HTTP method, name, URL path, and authority required to access the API.
     */
    @Data
    public static class Permission implements Serializable {

        /**
         * HTTP method required to access the API endpoint.
         */
        @NotNull(message = "Permission api [method] not null!")
        private HttpMethod method;

        /**
         * Name of the API endpoint.
         */
        @NotBlank(message = "Permission api [name] not blank!")
        private String name;

        /**
         * URL path of the API endpoint.
         */
        @NotBlank(message = "Permission api [url] not blank!")
        private String path;

        /**
         * Authority required to access the API endpoint.
         */
        @NotBlank(message = "Permission api [role] not blank!")
        private String authority;

        /**
         * Sets the authority for this permission and upgrades it to uppercase.
         *
         * @param authority original authority string
         */
        public void setAuthority(String authority) {
            this.authority = upgradeAuthorityUpperCase(authority);
        }

        /**
         * Upgrades the authority string to uppercase and adds prefix if necessary.
         *
         * @param authority original authority string
         * @return upgraded authority string
         */
        private String upgradeAuthorityUpperCase(String authority) {
            if (!StringUtils.hasLength(authority)) {
                return null;
            }
            String role = authority;
            if (!authority.startsWith(MenusService.AUTHORITY_PREFIX)) {
                role = MenusService.AUTHORITY_PREFIX + "BTN_" +
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, authority);
            }
            role = role.toUpperCase();
            return role;
        }
    }
}
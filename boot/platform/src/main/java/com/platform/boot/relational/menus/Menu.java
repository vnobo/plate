package com.platform.boot.relational.menus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.CaseFormat;
import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.annotation.*;
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


    @Id
    private Integer id;

    private String code;

    private String pcode;

    private String tenantCode;

    @NotNull(message = "Menu type cannot be null!")
    private MenuType type;

    @NotBlank(message = "Authority cannot be blank!")
    @Pattern(regexp = "^[a-zA-Z_]{1,50}$", message = "Authority can only contain English letters or '_' symbols.")
    private String authority;

    @NotBlank(message = "Name cannot be blank!")
    private String name;

    private String path;

    private Short sort;

    private JsonNode extend;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    public void setAuthority(String authority) {
        this.authority = upgradeAuthorityUpperCase(authority);
    }

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

    @JsonGetter
    public Set<Permission> getPermissions() {
        return Optional.ofNullable(this.getExtend()).map(node -> node.get("permissions"))
                .map(node -> ContextUtils.OBJECT_MAPPER.convertValue(node, new TypeReference<Set<Permission>>() {
                })).orElse(null);
    }

    @JsonGetter
    public String getIcons() {
        return Optional.ofNullable(this.getExtend()).map(node -> node.get("icons"))
                .map(JsonNode::asText).orElse(null);
    }

    enum MenuType {
        /**
         * folder
         */
        FOLDER,
        /**
         * menu
         */
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

    @Data
    public static class Permission implements Serializable {

        @NotNull(message = "Permission api [method] not null!")
        private HttpMethod method;

        @NotBlank(message = "Permission api [name] not blank!")
        private String name;

        @NotBlank(message = "Permission api [url] not blank!")
        private String path;

        @NotBlank(message = "Permission api [role] not blank!")
        private String authority;

        public void setAuthority(String authority) {
            this.authority = upgradeAuthorityUpperCase(authority);
        }

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
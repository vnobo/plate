package com.plate.boot.relational.menus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.CaseFormat;
import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

import java.security.Permission;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
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

    private Short sortNo;

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
        MENU,
        /**
         * 链接
         */
        LINK,
        /**
         * 接口
         */
        API;
    }

}
package com.plate.boot.relational.menus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.google.common.base.CaseFormat;
import com.plate.boot.commons.base.AbstractEntity;
import com.plate.boot.commons.utils.ContextUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.security.Permission;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a menu entity within an application, encapsulating details about menu items including
 * their type, associated permissions, hierarchy, and metadata for display and management purposes.
 * This class integrates with JPA annotations for database persistence and utilizes Logbook's {@link Data}
 * annotation for boilerplate code reduction related to getters, setters, equals, hash, and toString methods.
 * It also features custom methods to handle specific business logic such as authority formatting and
 * extraction of additional properties stored as JSON.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_menus")
public class Menu extends AbstractEntity<Integer> {

    /**
     * parent code
     */
    protected UUID pcode;

    /**
     * Represents the type of menu item.
     * <p>
     * This enumeration defines the different categories a menu item can fall into:
     * <ul>
     *   <li>{@link MenuType#FOLDER}: Denotes a folder that can contain other menu items.</li>
     *   <li>{@link MenuType#MENU}: Represents a standard menu option that can be interacted with directly.</li>
     *   <li>{@link MenuType#LINK}: Specifies a menu item that links to an external resource or URL.</li>
     *   <li>{@link MenuType#API}: Indicates a menu item that is associated with an API endpoint.</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "Menu type cannot be null!")
    private MenuType type;

    /**
     * Represents the authority associated with a menu item.
     * This field ensures that the authority adheres to specific validation rules:
     * - It must not be blank.
     * - It can only include English letters or '_' symbols, with a length constraint of 1 to 256 characters.
     * The authority follows a predefined pattern to maintain consistency and security within the system's permission structure.
     */
    @NotBlank(message = "Authority cannot be blank!")
    @Pattern(regexp = "^[a-zA-Z_]{1,256}$", message = "Authority can only contain English letters or '_' symbols.")
    private String authority;

    /**
     * The name of the menu item.
     * This field must not be blank; otherwise, a validation error will occur with the message "Name cannot be blank!".
     * It represents the display name or title of the menu entry within the application's navigation or interface.
     */
    @NotBlank(message = "Name cannot be blank!")
    private String name;

    /**
     * The path variable represents the route or URL path associated with a menu item.
     * It defines the specific path that can be accessed within the application's routing system.
     * This field is crucial for determining the navigation structure and linking menu items to their respective functionalities.
     */
    private String path;

    /**
     * Represents the sorting order of a menu item within a list or hierarchy.
     * A higher number indicates a lower priority in the sorting sequence.
     * Typically used for organizing the display order of menu items.
     */
    private Short sortNo;

    /**
     * Sets the authority for the menu item.
     * This method enhances the provided authority by prepending a prefix if necessary and converting it to uppercase.
     * It delegates the actual transformation to the {@link #upgradeAuthorityUpperCase(String)} method.
     *
     * @param authority The authority string to be set. It can be in any format but will be standardized.
     */
    public void setAuthority(String authority) {
        this.authority = upgradeAuthorityUpperCase(authority);
    }

    /**
     * Converts the provided authority string to uppercase and prefixes it with 'ROLE_' if not already present,
     * following a conversion from lowerCamelCase to UPPER_UNDERSCORE format. If the input is empty, returns null.
     *
     * @param authority The authority string to be upgraded. Expected in lowerCamelCase if not prefixed.
     * @return The upgraded authority string in UPPER_UNDERSCORE format, prefixed with 'ROLE_' if necessary, or null if input was empty.
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
     * Retrieves the set of permissions associated with the menu item.
     * This method checks if the 'extend' field is present and contains a 'permissions' node.
     * If found, it converts the value of 'permissions' to a set of Permission objects using an ObjectMapper.
     * Returns null if the 'permissions' node is not present or 'extend' is null.
     *
     * @return A set of Permission objects representing the permissions, or null if not available.
     */
    @JsonGetter
    public Set<Permission> getPermissions() {
        return Optional.ofNullable(this.getExtend()).map(node -> node.get("permissions"))
                .map(node -> ContextUtils.OBJECT_MAPPER.convertValue(node, new TypeReference<Set<Permission>>() {
                })).orElse(null);
    }

    /**
     * Retrieves the icons associated with the menu item.
     * This method inspects the 'extend' property, which is expected to be a JSON node,
     * attempting to extract a text value under the key 'icons'. If the 'extend' property or the 'icons'
     * key within it is not present, the method returns null.
     *
     * @return A string representing the icons of the menu item, or null if not found.
     */
    @JsonGetter
    public String getIcons() {
        return Optional.ofNullable(this.getExtend()).map(node -> node.get("icons"))
                .map(JsonNode::asString).orElse(null);
    }

    /**
     * Enumerates the different types of menu items that can be defined within a system.
     * Each constant represents a distinct category with specific functionalities or purposes.
     */
    enum MenuType {
        /**
         * Represents a category for folder-type menu items within the system.
         * This type signifies a menu item that can contain other sub-menu items, organizing them in a hierarchical structure.
         */
        FOLDER,
        /**
         * Represents a menu item within a system's navigation structure.
         * This could be a collection of sub-items, an actionable menu option, a link to another page, or an API endpoint.
         * It is a part of the {@link MenuType} enumeration, which categorizes various types of menu elements.
         */
        MENU,
        /**
         * Represents a type of menu item that serves as a hyperlink to external resources or internal system pages.
         * Unlike {@link MenuType#FOLDER} and {@link MenuType#MENU}, which can contain nested items, {@code LINK} is a terminal item
         * intended to redirect users when selected.
         */
        LINK,
        /**
         * Represents an API-related menu item within the system.
         * This type signifies that the menu item is associated with an API call or functionality.
         */
        API
    }

}
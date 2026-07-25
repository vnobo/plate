package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.base.AbstractEntity;
import com.plate.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

/**
 * Entity class representing a permission (authority) assigned to a user group.
 * Implements {@link GrantedAuthority} so group authorities can be resolved as Spring Security authorities.
 * Maps to the {@code se_group_authorities} table and extends {@link AbstractEntity} for common
 * entity attributes such as the primary key, tenant isolation, and audit fields.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_group_authorities")
public class GroupAuthority extends AbstractEntity<Integer> implements GrantedAuthority, BaseEntity<Integer> {

    /**
     * The group code this authority belongs to; must not be {@code null}.
     */
    @NotNull(message = "Group authority [groupCode] cannot be empty!")
    private UUID groupCode;

    /**
     * The authority (permission) granted to the group; must not be blank.
     */
    @NotBlank(message = "Group authority [authority] cannot be empty!")
    private String authority;

}
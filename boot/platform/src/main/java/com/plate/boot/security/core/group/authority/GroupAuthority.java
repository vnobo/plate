package com.plate.boot.security.core.group.authority;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.base.AbstractEntity;
import com.plate.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

/**
 * Entity class representing a group authority.
 * This class implements GrantedAuthority interface.
 * It is annotated with \@Table to map it to the "se_group_authorities" table in the database.
 * It uses Lombok annotations for boilerplate code reduction.
 * <p>
 * The class extends AbstractEntity to inherit common entity properties.
 * It includes validation constraints for its fields.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_group_authorities")
public class GroupAuthority extends AbstractEntity<Integer> implements GrantedAuthority, BaseEntity<Integer> {

    /**
     * The unique code of the group.
     * It is annotated with \@NotBlank to ensure it is not null or empty.
     */
    @NotNull(message = "Group authority [groupCode] cannot be empty!")
    private UUID groupCode;

    /**
     * The authority granted to the group.
     * It is annotated with \@NotBlank to ensure it is not null or empty.
     */
    @NotBlank(message = "Group authority [authority] cannot be empty!")
    private String authority;

    /**
     * Data tenant code
     */
    @Transient
    private String tenantCode;

    /**
     * Data entity extend,Json column
     */
    @Transient
    private JsonNode extend;
}
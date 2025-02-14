package com.plate.boot.security.core.group.authority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Table("se_group_authorities")
public class GroupAuthority extends AbstractEntity<Integer> implements GrantedAuthority {

    /**
     * The unique code of the group.
     * It is annotated with \@NotBlank to ensure it is not null or empty.
     */
    @NotBlank(message = "Group authority [groupCode] cannot be empty!")
    private UUID groupCode;

    /**
     * The authority granted to the group.
     * It is annotated with \@NotBlank to ensure it is not null or empty.
     */
    @NotBlank(message = "Group authority [authority] cannot be empty!")
    private String authority;

    /**
     * Constructs a new GroupAuthority with the specified group code and authority.
     *
     * @param groupCode the unique code of the group
     * @param authority the authority granted to the group
     */
    public GroupAuthority(UUID groupCode, String authority) {
        this.groupCode = groupCode;
        this.authority = authority;
    }

    /**
     * Returns the tenant code associated with the group authority.
     * This method is annotated with \@JsonIgnore to exclude it from JSON serialization.
     *
     * @return the tenant code
     */
    @JsonIgnore
    @Override
    public String getTenantCode() {
        return this.tenantCode;
    }
}
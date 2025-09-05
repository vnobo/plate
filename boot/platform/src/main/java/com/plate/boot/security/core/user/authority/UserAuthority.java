package com.plate.boot.security.core.user.authority;

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
 * Entity class representing a user authority.
 * This class implements GrantedAuthority and BaseEntity interfaces.
 * It is annotated with \@Table to map it to the "se_authorities" table in the database.
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
@Table("se_authorities")
public class UserAuthority extends AbstractEntity<Integer> implements GrantedAuthority, BaseEntity<Integer> {

    /**
     * The unique code of the user.
     * It is annotated with \@NotBlank to ensure it is not null or empty.
     */
    @NotNull(message = "User entity [userCode] cannot be empty!")
    private UUID userCode;

    /**
     * The authority granted to the user.
     * It is annotated with \@NotBlank to ensure it is not null or empty.
     */
    @NotBlank(message = "User entity [authority] cannot be empty!")
    private String authority;

}
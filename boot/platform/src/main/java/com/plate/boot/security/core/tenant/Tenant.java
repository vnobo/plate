package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Represents a tenant entity in the system.
 * This class is annotated with Lombok's \@Data to generate boilerplate code such as getters, setters, toString, equals, and hashCode methods.
 * It is also mapped to the "se_tenants" table in the database.
 * <p>
 * The class includes validation constraints and auditing fields.
 * <p>
 * \@author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_tenants")
public class Tenant extends AbstractEntity<Integer> {

    /**
     * The parent code of the tenant.
     * This field is mandatory and cannot be blank.
     */
    @NotNull(message = "Tenant parent code [pcode] cannot be empty!")
    private UUID pcode;

    /**
     * The name of the tenant.
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Tenant name [name] cannot be empty!")
    private String name;

    /**
     * The description of the tenant.
     */
    private String description;

}
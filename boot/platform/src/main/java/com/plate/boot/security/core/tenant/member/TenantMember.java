package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Represents a member associated with a tenant within the system.
 * <p>
 * This class extends {@link AbstractEntity} to inherit common entity properties
 * and adds specific fields relevant to tenant membership management.
 * The entity is mapped to the database table {@code se_tenant_members}.
 *
 * @see AbstractEntity Base class providing common entity properties like ID, creation timestamp, etc.
 */
@EqualsAndHashCode(callSuper = true) // Generates equals and hashCode with superclass fields
@Data // Lombok annotation to generate getters, setters, toString, etc.
@Table("se_tenant_members") // Specifies the database table name for this entity
public class TenantMember extends AbstractEntity<Long> {

    /**
     * Unique identifier for the associated user in UUID format.
     * <p>
     * This field establishes a reference to the system user and must be non-blank.
     * The UUID format ensures global uniqueness across the system.
     *
     * @see UUID &#064;NotBlank  Validation constraint ensuring the value cannot be empty
     */
    @NotNull(message = "The user code [userCode] cannot be empty!")
    private UUID userCode;

    /**
     * Activation status flag for the tenant membership.
     * <p>
     * Uses Boolean wrapper type to allow for three-state representation:
     * - {@code true}: Membership is active
     * - {@code false}: Membership is inactive
     * <p>
     * Default database mapping will typically interpret null as equivalent to false,
     * but this should be explicitly handled in the application logic.
     */
    @NotNull(message = "The enabled flag [enabled] cannot be null!")
    private Boolean enabled;
}

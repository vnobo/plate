package com.plate.boot.security.core.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents a tenant entity in the system.
 * This class is annotated with Lombok's \@Data to generate boilerplate code such as getters, setters, toString, equals, and hashCode methods.
 * It is also mapped to the "se_tenants" table in the database.
 * <p>
 * The class includes validation constraints and auditing fields.
 * <p>
 * \@author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_tenants")
public class Tenant implements BaseEntity<Integer> {

    /**
     * The unique identifier for the tenant.
     */
    @Id
    private Integer id;

    /**
     * The code of the tenant.
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Tenant [code] cannot be empty!")
    private String code;

    /**
     * The parent code of the tenant.
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Tenant parent code [pcode] cannot be empty!")
    private String pcode;

    /**
     * The name of the tenant.
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Tenant name [name] cannot be empty!")
    private String name;

    /**
     * Additional information about the tenant in JSON format.
     */
    private JsonNode extend;

    /**
     * The user who created the tenant.
     * This field is automatically populated by the auditing framework.
     */
    @CreatedBy
    private UserAuditor creator;

    /**
     * The user who last modified the tenant.
     * This field is automatically populated by the auditing framework.
     */
    @LastModifiedBy
    private UserAuditor updater;

    /**
     * The timestamp when the tenant was created.
     * This field is automatically populated by the auditing framework.
     */
    @CreatedDate
    private LocalDateTime createdTime;

    /**
     * The timestamp when the tenant was last modified.
     * This field is automatically populated by the auditing framework.
     */
    @LastModifiedDate
    private LocalDateTime updatedTime;
}
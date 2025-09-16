package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Group Entity
 * Represents a group in the system, extending AbstractEntity for common entity functionality
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_groups")
public class Group extends AbstractEntity<Integer> {

    /**
     * The code of the group, cannot be blank
     */
    @NotNull(message = "Group parent [pcode] cannot be empty!")
    private UUID pcode;

    /**
     * The name of the group, cannot be blank
     */
    @NotBlank(message = "Group [name] cannot be empty!")
    private String name;

}
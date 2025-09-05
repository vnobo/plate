package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

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
     * The name of the group, cannot be blank
     */
    @NotBlank(message = "Group [name] cannot be empty!")
    private String name;

}
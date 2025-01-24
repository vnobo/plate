package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_groups")
public class Group extends AbstractEntity<Integer> {

    @NotBlank(message = " Rules [name] not be empty!")
    private String name;

}
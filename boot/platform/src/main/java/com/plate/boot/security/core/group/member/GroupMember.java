package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_group_members")
public class GroupMember extends AbstractEntity<Long> {

    @NotBlank(message = "Rule [groupCode] not be empty!")
    private UUID groupCode;

    @NotBlank(message = "User [username]not be empty!")
    private UUID userCode;
}
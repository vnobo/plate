package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Represents a member within a group, associating a user with a specific group identified by codes.
 * This class extends {@link AbstractEntity} to inherit common entity attributes and behaviors.
 * It adds validation constraints on the `groupCode` and `userCode` fields to ensure they are not blank,
 * enhancing data integrity.
 *
 * <p>
 * The `groupCode` field corresponds to the unique identifier of the group to which the member belongs.
 * The `userCode` field represents the unique identifier of the user who is a member of the group.
 *
 * @see AbstractEntity
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_group_members")
public class GroupMember extends AbstractEntity<Long> {

    /**
     * The unique identifier of the group to which the member belongs.
     */
    @NotNull(message = "Group member [groupCode] cannot be empty!")
    private UUID groupCode;

    /**
     * The unique identifier of the user who is a member of the group.
     */
    @NotNull(message = "Group member [username] cannot be empty!")
    private UUID userCode;
}
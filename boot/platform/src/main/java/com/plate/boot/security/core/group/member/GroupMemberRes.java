package com.plate.boot.security.core.group.member;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.base.BaseResEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a response model for group member details, extending the {@link GroupMember} entity.
 * This class enhances the basic member information by including the member's name and additional
 * group-specific extension data in a JSON format.
 *
 * <p>
 * The `name` field provides the human-readable name of the group member, supplementing the
 * identifier-based fields inherited from {@link GroupMember}.
 *
 * <p>
 * The `groupExtend` field is a JsonNode object containing extended metadata or attributes associated
 * with the group to which the member belongs. This flexible field can hold various structured data
 * relevant to group configurations or permissions.
 *
 * <p>
 * This class is particularly useful in API responses where a more descriptive representation of a
 * group member is required beyond just their identifiers, facilitating a richer understanding of
 * group membership contexts.
 *
 * @see GroupMember
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberRes extends GroupMember implements BaseResEntity<Long> {

    private String name;

    private JsonNode groupExtend;

}
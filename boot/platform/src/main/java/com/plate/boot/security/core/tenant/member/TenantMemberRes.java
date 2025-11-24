package com.plate.boot.security.core.tenant.member;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tools.jackson.databind.JsonNode;

/**
 * Represents a response for tenant member operations, extending the TenantMember class.
 * This class includes additional fields for the tenant member's name and extended attributes.
 * It provides functionality to encapsulate tenant member-related response data.
 * <p>
 * Author: <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantMemberRes extends TenantMember {

    /**
     * The name of the tenant member.
     * This field holds the display name associated with the tenant member.
     */
    private String name;

    /**
     * A JSON node containing extended attributes for the tenant member.
     * This field can store additional metadata or custom attributes in JSON format.
     */
    private JsonNode extend;

}
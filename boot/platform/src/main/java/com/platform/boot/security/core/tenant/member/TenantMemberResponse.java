package com.platform.boot.security.core.tenant.member;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantMemberResponse extends TenantMember {

    private String name;

    private JsonNode extend;

}
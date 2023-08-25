package com.platform.boot.security.tenant.member;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantMemberResponse extends TenantMember {

    private String userName;

    private String tenantName;

    private JsonNode tenantExtend;

}
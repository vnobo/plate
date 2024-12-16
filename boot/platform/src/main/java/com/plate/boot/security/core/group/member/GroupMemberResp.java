package com.plate.boot.security.core.group.member;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberResp extends GroupMember {

    private String name;

    private JsonNode extend;

}
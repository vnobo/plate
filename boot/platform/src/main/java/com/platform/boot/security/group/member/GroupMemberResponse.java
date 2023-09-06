package com.platform.boot.security.group.member;

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
public class GroupMemberResponse extends GroupMember {

    private String username;
    private String loginName;
    private String groupName;
    private JsonNode groupExtend;

}
package com.platform.boot.security.core.group.member;

import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_group_members")
public class GroupMember implements BaseEntity<Long> {

    @Id
    private Long id;

    private String code;

    @NotBlank(message = "Rule [groupCode] not be empty!")
    private String groupCode;

    @NotBlank(message = "User [username]not be empty!")
    private String userCode;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    @Override
    public void setCode(String code) {
        this.code = code.startsWith("GM") ? code : "GM" + code;
    }
}
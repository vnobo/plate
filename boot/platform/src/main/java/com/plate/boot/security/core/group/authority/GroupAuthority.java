package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@NoArgsConstructor
@Table("se_group_authorities")
public class GroupAuthority implements GrantedAuthority, BaseEntity<Integer> {

    @Id
    private Integer id;

    private String code;

    @NotBlank(message = "Role [groupCode] can not be empty!")
    private String groupCode;

    @NotBlank(message = "Role [authority]can not be empty!")
    private String authority;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    public GroupAuthority(String groupCode, String authority) {
        this.groupCode = groupCode;
        this.authority = authority;
    }

    @Override
    public void setCode(String code) {
        this.code = code.startsWith("GA") ? code : "GA" + code;
    }
}
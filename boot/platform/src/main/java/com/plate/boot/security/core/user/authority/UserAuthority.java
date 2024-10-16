package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_authorities")
public class UserAuthority implements GrantedAuthority, BaseEntity<Integer> {

    @Id
    private Integer id;

    private String code;

    @NotBlank(message = "用户[userCode]不能为空!")
    private String userCode;

    @NotBlank(message = "权限[authority]不能为空!")
    private String authority;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    public UserAuthority() {
    }

    public UserAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public void setCode(String code) {
        this.code = code.startsWith("UA") ? code : "UA" + code;
    }
}
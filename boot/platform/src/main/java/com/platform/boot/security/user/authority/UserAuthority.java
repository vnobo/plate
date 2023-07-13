package com.platform.boot.security.user.authority;

import com.platform.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_authorities")
public class UserAuthority implements GrantedAuthority, BaseEntity<Integer> {

    @Id
    private Integer id;

    @NotBlank(message = "用户[userCode]不能为空!")
    private String username;

    @NotBlank(message = "权限[authority]不能为空!")
    private String authority;

}
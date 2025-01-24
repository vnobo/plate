package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.base.AbstractEntity;
import com.plate.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_authorities")
public class UserAuthority extends AbstractEntity<Integer> implements GrantedAuthority, BaseEntity<Integer> {

    @NotBlank(message = "用户[userCode]不能为空!")
    private UUID userCode;

    @NotBlank(message = "权限[authority]不能为空!")
    private String authority;

}
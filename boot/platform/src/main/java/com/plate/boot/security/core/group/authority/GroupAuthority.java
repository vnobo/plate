package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Table("se_group_authorities")
public class GroupAuthority extends AbstractEntity<Integer> implements GrantedAuthority {



    @NotBlank(message = "Role [groupCode] can not be empty!")
    private UUID groupCode;

    @NotBlank(message = "Role [authority]can not be empty!")
    private String authority;

    public GroupAuthority(UUID groupCode, String authority) {
        this.groupCode = groupCode;
        this.authority = authority;
    }
}
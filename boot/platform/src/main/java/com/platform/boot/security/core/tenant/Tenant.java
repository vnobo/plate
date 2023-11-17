package com.platform.boot.security.core.tenant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 * //
 */
@Data
@Table("se_tenants")
public class Tenant implements BaseEntity<Integer> {

    @Id
    private Integer id;

    @NotBlank(message = "租户编码[code]不能为空!")
    private String code;

    @NotBlank(message = "租户父级[pcode]不能为空!")
    private String pcode;

    @NotBlank(message = "租户名[name]不能为空!")
    private String name;

    private JsonNode extend;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return ObjectUtils.isEmpty(getId());
    }
}
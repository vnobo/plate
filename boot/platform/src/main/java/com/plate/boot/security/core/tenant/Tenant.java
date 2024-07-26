package com.plate.boot.security.core.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

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

    @NotBlank(message = "编码[code]不能为空!")
    private String code;

    @NotBlank(message = "父级[pcode]不能为空!")
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
    public void setCode(String code) {
        this.code = code.startsWith("T") ? code : "T" + code;
    }
}
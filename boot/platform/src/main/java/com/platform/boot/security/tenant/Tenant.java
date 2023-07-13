package com.platform.boot.security.tenant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 * //
 */
@Data
@Table("se_tenants")
public class Tenant implements Serializable, Persistable<Integer> {

    @Id
    private Integer id;

    @NotNull(message = "租户编码[code]不能为空!")
    private String code;

    @NotNull(message = "租户父级[pcode]不能为空!")
    private String pcode;

    @NotBlank(message = "租户名[name]不能为空!")
    private String name;

    private JsonNode extend;

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
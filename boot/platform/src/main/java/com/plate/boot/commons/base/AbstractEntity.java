package com.plate.boot.commons.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.security.core.UserAuditor;
import lombok.Data;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
public abstract class AbstractEntity<T> implements BaseEntity<T> {

    @Id
    protected T id;

    protected String code;

    protected String tenantCode;

    protected JsonNode extend;

    @CreatedBy
    protected UserAuditor creator;

    @LastModifiedBy
    protected UserAuditor updater;

    @LastModifiedDate
    protected LocalDateTime updatedTime;

    @CreatedDate
    protected LocalDateTime createdTime;

    /**
     * Support query for json column
     */
    @Transient
    protected Map<String, Object> query;

    /**
     * Support full text search for tsvector column
     */
    @Transient
    protected String search;

    /**
     * Support security code for sensitive data
     */
    @Transient
    protected String securityCode;

}

package com.plate.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.security.core.UserAuditor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * This class, AbstractEntity, is a base class for all data entities in the system.
 * It provides common fields and methods for all data entities, such as id, code, tenantCode,
 * extend, creator, updater, updatedTime, createdTime, from, search, and securityCode.
 * <p>
 * The id field is the unique identifier of the data entity. The code field is the data entity code,
 * and the tenantCode field is the data tenant code.
 * The extend field is a JsonNode object that represents the data entity extend,Json column.
 * The creator field is the data entity create operator, and the updater field is the data entity update operator.
 * The updatedTime field is the data entity update time,timestamp column,
 * and the createdTime field is the data entity create time, timestamp column.
 * The from field is a Map object that supports from for json column,
 * and the search field is a String object that supports full text search for tsvector column.
 * The securityCode field is a String object that supports security code for sensitive data.
 * <p>
 * This class uses the Lombok library to simplify the code and make it more readable.
 * The @Data annotation generates getters, setters, and equals/hashCode methods for all fields in the class.
 * The @Id, @CreatedBy, @LastModifiedBy, @LastModifiedDate, and @CreatedDate annotations provide metadata for the fields,
 * such as their unique identifier, creation time, and update time.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class AbstractEntity<T> implements BaseEntity<T> {

    @Id
    @EqualsAndHashCode.Include
    protected T id;

    /**
     * Data entity code
     */
    protected UUID code;

    /**
     * Data tenant code
     */
    protected String tenantCode;

    /**
     * Data entity extend,Json column
     */
    protected JsonNode extend;

    /**
     * Data entity create operator
     * use User. Class code property
     */
    @CreatedBy
    protected UserAuditor createdBy;

    /**
     * Data entity create time, timestamp column
     */
    @CreatedDate
    protected LocalDateTime createdAt;

    /**
     * Data entity update operator
     * use User.class code property
     */
    @LastModifiedBy
    protected UserAuditor updatedBy;

    /**
     * Data entity update time,timestamp column
     */
    @LastModifiedDate
    protected LocalDateTime updatedAt;


    /**
     * Support from for json column
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
    @JsonIgnore
    @Transient
    protected String securityCode;

    @JsonIgnore
    public String getSearch() {
        return search;
    }

    @JsonIgnore
    public Map<String, Object> getQuery() {
        return query;
    }
}

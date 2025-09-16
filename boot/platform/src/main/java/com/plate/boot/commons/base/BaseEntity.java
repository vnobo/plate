package com.plate.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.query.QueryHelper;
import com.plate.boot.commons.query.QueryJsonHelper;
import com.plate.boot.commons.utils.ContextUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Represents the base entity contract for entities that require common functionality
 * such as having a unique code, being serializable, and perishable with a generic type identifier.
 * Implementing classes should provide concrete behavior for these base operations.
 */
public interface BaseEntity<T> extends Serializable, Persistable<T> {
    /**
     * Support from for json column
     */
    @JsonIgnore
    default Map<String, Object> getQuery() {
        return null;
    }

    /**
     * Support full text search for tsvector column
     */
    @JsonIgnore
    default String getSearch() {
        return null;
    }

    /**
     * Support security code for sensitive data
     */
    @JsonIgnore
    default UUID getSecurityCode() {
        return null;
    }

    /**
     * Retrieves the unique code assigned to the entity.
     * This code serves as a distinct identifier for the entity within the system.
     *
     * @return The unique identifier (UUID) of the entity.
     */
    default <E> E getCode() {
        return null;
    }

    /**
     * Sets the unique code for an entity.
     * This method is intended to assign or update the code attribute of an entity,
     * which serves as a unique identifier in accordance with the BaseEntity interface.
     * The implementation should handle the logic of how the code is generated or validated,
     * based on specific business rules, which may involve prefixing, formatting, or checking for uniqueness.
     *
     * @param code The code to be set for the entity. It could be a plain string or follow a predefined format.
     */
    default void setCode(UUID code) {
    }

    /**
     * Determines whether the entity is new, typically indicating it has not been persisted yet.
     * This is assessed by checking if the entity's identifier ({@code getId}) is empty.
     * If the entity is determined to be new, a unique code is generated using {@link ContextUtils#nextId()}
     * and assigned to the entity via {@link #setCode(UUID)}.
     *
     * @return {@code true} if the entity is considered new (i.e., lacks an identifier), otherwise {@code false}.
     */
    @Override
    @JsonIgnore
    default boolean isNew() {
        if (Optional.ofNullable(getCode()).isEmpty()) {
            setCode(ContextUtils.nextId());
        }
        return Optional.ofNullable(getId()).isEmpty();
    }

    /**
     * Constructs a {@link Criteria} instance based on the current entity,
     * allowing for the specification of properties to be excluded from criteria creation.
     *
     * @param skipKeys A {@link Collection} of {@link String} property keys to be skipped when building the criteria.
     *                 These properties will not be included in the generated criteria.
     * @return A {@link Criteria} object tailored according to the current entity,
     * excluding the properties specified in the {@code skipKeys} collection.
     */
    default Criteria criteria(Collection<String> skipKeys) {
        return QueryHelper.criteria(this, skipKeys);
    }

    /**
     * Constructs a QueryFragment based on the current entity's properties and conditions,
     * allowing for customization of the SQL from by specifying properties to exclude.
     *
     * @param skipKeys A collection of String property names indicating which properties
     *                 should not be included in the generated SQL from. This can be useful
     *                 for skipping sensitive or unnecessary fields.
     * @return A QueryFragment object containing the SQL fragment and parameters necessary
     * to form a part of an SQL from. The SQL fragment represents a conditional
     * part of the from (e.g., WHERE clause), and the parameters are mapped to
     * prevent SQL injection, ensuring secure from execution.
     */
    default QueryFragment query(Collection<String> skipKeys) {
        var criteria = criteria(skipKeys);
        var tableName = QueryHelper.annotationTableName(this);
        var fragment = QueryFragment.from(tableName).condition(QueryFragment.Condition.of(criteria));
        if (!ObjectUtils.isEmpty(getSearch())) {
            fragment.ts("text_search", getSearch());
        }
        if (!ObjectUtils.isEmpty(getQuery())) {
            fragment.condition(QueryJsonHelper.queryJson(getQuery(), null));
        }
        if (!ObjectUtils.isEmpty(getSecurityCode())) {
            fragment.isEq("tenantCode", getSecurityCode());
        }
        return fragment;
    }

    default QueryFragment query() {
        return query(List.of());
    }
}
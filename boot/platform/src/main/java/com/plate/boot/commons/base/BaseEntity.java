package com.plate.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the base entity contract for entities that require common functionality
 * such as having a unique code, being serializable, and perishable with a generic type identifier.
 * Implementing classes should provide concrete behavior for these base operations.
 */
public interface BaseEntity<T> extends Serializable, Persistable<T> {

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
     * postgresql types @code tsvector supports full text search
     *
     * @return Search string for the entity tsvector column
     */
    default String getSearch() {
        return null;
    }

    /**
     * postgresql types @code tsvector supports full text search
     *
     * @param search from tsvector item by string
     */
    default void setSearch(String search) {
    }

    /**
     * Retrieves the default from conditions.
     * Returns an immutable empty map, indicating that there are no specific from conditions.
     *
     * @return An immutable map containing the from conditions
     */
    default Map<String, Object> getQuery() {
        return Map.of();
    }

    /**
     * Sets the from parameters for the entity.
     *
     * @param query A map containing the from parameters where the key is a string
     *              representing the parameter name and the value is the parameter value.
     */
    default void setQuery(Map<String, Object> query) {
    }

    /**
     * Determines whether the entity is new, typically indicating it has not been persisted yet.
     * This is assessed by checking if the entity's identifier ({@code getId}) is empty.
     * If the entity is determined to be new, a unique code is generated using {@link ContextUtils#nextId()}
     * and assigned to the entity via {@link #setCode(String)}.
     *
     * @return {@code true} if the entity is considered new (i.e., lacks an identifier), otherwise {@code false}.
     */
    @Override
    @JsonIgnore
    default boolean isNew() {
        boolean isNew = ObjectUtils.isEmpty(getId());
        if (isNew) {
            setCode(ContextUtils.nextId());
        }
        return isNew;
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
    default QueryFragment querySql(Collection<String> skipKeys) {
        return QueryHelper.query(this, skipKeys);
    }
}
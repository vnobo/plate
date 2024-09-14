package com.plate.boot.commons.utils.query;

import java.util.Map;

/**
 * Represents a single condition for a database query, encapsulating the operation,
 * the corresponding SQL fragment, and the parameters required for the query.
 * <p>
 * This record is primarily used within SQL construction logic to dynamically
 * build WHERE clauses based on provided criteria, supporting various comparison
 * and set-based operations through its components.
 *
 * @param operation A Map.Entry consisting of a keyword indicating the type of operation (e.g., equality, range)
 *                  and a placeholder or specific SQL syntax related to the operation.
 * @param sql       The SQL fragment representing the condition without actual values,
 *                  with placeholders for parameters.
 * @param params    A map mapping parameter placeholders used in the SQL fragment to their intended values.
 */
public record QueryCondition(
        String sql,
        Map<String, Object> params,
        Map.Entry<String, String> operation) {
}
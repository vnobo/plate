package com.plate.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Represents a logging request that extends the basic {@link Logger} functionality,
 * encapsulating details necessary for logging specific HTTP requests. This includes
 * query parameters, security codes, and provides utility methods for constructing
 * log entries compatible with the core {@link Logger} infrastructure.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerRequest extends Logger {

    private Map<String, Object> query;

    private String securityCode;

    /**
     * Constructs a new {@link LoggerRequest} instance with specified details.
     *
     * @param tenantCode The tenant code associated with the logging request.
     * @param operator   The operator (user or system component) initiating the request.
     * @param prefix     A prefix to categorize or namespace the log entry.
     * @param method     The HTTP method of the request (e.g., GET, POST).
     * @param status     The status of the request processing (e.g., success, error).
     * @param url        The URL path targeted by the request.
     * @param context    Additional contextual information in JSON format.
     * @return A configured {@link LoggerRequest} object ready for logging purposes.
     */
    public static LoggerRequest of(String tenantCode, String operator, String prefix,
                                   String method, String status, String url, JsonNode context) {
        LoggerRequest request = new LoggerRequest();
        request.setTenantCode(tenantCode);
        request.setOperator(operator);
        request.setPrefix(prefix);
        request.setUrl(url);
        request.setMethod(method);
        request.setStatus(status);
        request.setContext(context);
        return request;
    }

    public Logger toLogger() {
        return BeanUtils.copyProperties(this, Logger.class);
    }

    public QueryFragment buildQueryFragment() {
        return QueryHelper.query(this, List.of(), null);
    }
}
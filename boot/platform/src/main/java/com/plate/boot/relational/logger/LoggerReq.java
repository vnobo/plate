package com.plate.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.MethodType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a logging request that extends the basic {@link Logger} functionality,
 * encapsulating details necessary for logging specific HTTP requests. This includes
 * from parameters, security codes, and provides utility methods for constructing
 * log entries compatible with the core {@link Logger} infrastructure.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerReq extends Logger {

    /**
     * Constructs a new {@link LoggerReq} instance with specified details.
     *
     * @param tenantCode The tenant code associated with the logging request.
     * @param operator   The operator (user or system component) initiating the request.
     * @param prefix     A prefix to categorize or namespace the log entry.
     * @param method     The HTTP method of the request (e.g., GET, POST).
     * @param status     The status of the request processing (e.g., success, error).
     * @param url        The URL path targeted by the request.
     * @param context    Additional contextual information in JSON format.
     * @return A configured {@link LoggerReq} object ready for logging purposes.
     */
    public static LoggerReq of(String tenantCode, String operator, String prefix,
                               String method, String status, String url, JsonNode context) {
        LoggerReq request = new LoggerReq();
        request.setTenantCode(tenantCode);
        request.setOperator(operator);
        request.setPrefix(prefix);
        request.setUrl(url);
        request.setMethod(MethodType.value(method));
        request.setStatus(status);
        request.setContext(context);
        return request;
    }

    /**
     * Converts this {@link LoggerReq} instance into a {@link Logger} instance.
     * Utilizes bean copying to transfer properties from the request to the logger entity,
     * which is suitable for persisting logging activities in the system.
     *
     * @return A {@link Logger} entity mirroring the properties of this request, ready for database storage.
     */
    public Logger toLogger() {
        return BeanUtils.copyProperties(this, Logger.class);
    }

}
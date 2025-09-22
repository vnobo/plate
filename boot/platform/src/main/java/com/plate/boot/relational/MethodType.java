package com.plate.boot.relational;

import com.plate.boot.commons.exception.RestServerException;
import org.springframework.lang.Nullable;

/**
 * HTTP method type enumeration that defines common HTTP method types.
 *
 * @author Alex bob(<a href="https://github.com/vnobo">AlexBob</a>)
 */
public enum MethodType {

    POST,
    PUT,
    DELETE,
    UNKNOWN;

    private static final MethodType[] VALUES;

    static {
        VALUES = values();
    }

    /**
     * Gets the corresponding MethodType enum value for the given string.
     * Throws RestServerException if no matching enum value is found.
     *
     * @param statusCode String representing the HTTP method type
     * @return Matching MethodType enum value
     * @throws RestServerException when statusCode cannot be resolved to a valid MethodType
     */
    public static MethodType value(String statusCode) {
        MethodType status = resolve(statusCode);
        if (status == null) {
            throw RestServerException.withMsg("This type is code [" + statusCode + "] error!",
                    new IllegalArgumentException("Code resolve error,is null value!"));
        }
        return status;
    }


    /**
     * Resolves the corresponding MethodType enum value for the given string.
     * Uses cached VALUES array instead of values() method to prevent array allocation overhead.
     *
     * @param statusCode String representing the HTTP method type
     * @return Matching MethodType enum value, or null if no match is found
     */
    @Nullable
    public static MethodType resolve(String statusCode) {
        // Use cached VALUES instead of values() to prevent array allocation.
        for (MethodType status : VALUES) {
            if (status.name().equals(statusCode)) {
                return status;
            }
        }
        return null;
    }

}
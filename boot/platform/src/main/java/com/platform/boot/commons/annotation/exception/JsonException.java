package com.platform.boot.commons.annotation.exception;

import java.io.IOException;

/**
 * The JsonException class is used to handle JSON exceptions.
 * It inherits from the RestServerException class.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 * @see RestServerException
 * @since 1.0
 */
public class JsonException extends RestServerException {

    /**
     * Constructor.
     *
     * @param jsonProcessingException JSON processing exception
     */
    public JsonException(IOException jsonProcessingException) {
        this(1101, jsonProcessingException);
    }

    /**
     * Constructor.
     *
     * @param status status code
     * @param msg    exception message
     */
    public JsonException(int status, Object msg) {
        super(status, msg);
    }

    /**
     * Static method that returns a JsonException object.
     *
     * @param jsonProcessingException JSON processing exception
     * @return JsonException object
     */
    public static JsonException withError(IOException jsonProcessingException) {
        return new JsonException(jsonProcessingException);
    }
}
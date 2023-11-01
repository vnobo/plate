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

    public JsonException(IOException jsonProcessingException) {
        this(5010, "Json processing exception", jsonProcessingException);
    }

    public JsonException(int status, String message, Object msg) {
        super(status, message, msg);
    }

    public static JsonException withError(IOException jsonProcessingException) {
        return new JsonException(jsonProcessingException);
    }
}
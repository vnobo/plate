package com.plate.boot.commons.exception;

import java.io.IOException;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public class JsonException extends RestServerException {

    public JsonException(IOException jsonProcessingException) {
        this(500, "Json processing exception", jsonProcessingException);
    }

    public JsonException(int status, String message, Object msg) {
        super(status, message, msg);
    }

    public static JsonException withError(IOException jsonProcessingException) {
        return new JsonException(jsonProcessingException);
    }
}
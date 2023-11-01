// Define the package name
package com.platform.boot.commons.annotation.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * This class represents a custom exception for the system.
 * It extends the RuntimeException class and implements Serializable.
 * It contains a message and a code to identify the error.
 * The class also provides static methods to create instances of the exception.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RestServerException extends RuntimeException implements Serializable {

    protected Object msg;
    protected int code;

    public RestServerException(int code, String message, Object msg) {
        super(message);
        this.msg = msg;
        this.code = code;
    }

    public static RestServerException withMsg(String message, Object msg) {
        return withMsg(5000, message, msg);
    }

    public static RestServerException withMsg(int code, String message, Object msg) {
        return new RestServerException(code, message, msg);
    }

}
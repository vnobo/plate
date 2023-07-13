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

    public RestServerException(int code, Object msg) {
        super(msg.toString());
        this.msg = msg;
        this.code = code;
    }

    /**
     * This method creates a new instance of RestServerException with the given code and message.
     *
     * @param code the code to identify the error
     * @param msg  the message to describe the error
     * @return a new instance of RestServerException
     */
    public static RestServerException withMsg(int code, Object msg) {
        return new RestServerException(code, msg);
    }

    /**
     * This method creates a new instance of RestServerException with the default code and the given message.
     *
     * @param msg the message to describe the error
     * @return a new instance of RestServerException
     */
    public static RestServerException withMsg(Object msg) {
        return withMsg(1000, msg);
    }
}
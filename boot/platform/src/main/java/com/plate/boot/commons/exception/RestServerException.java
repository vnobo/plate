// Define the package name
package com.plate.boot.commons.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
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
        return withMsg(500, message, msg);
    }

    public static RestServerException withMsg(int code, String message, Object msg) {
        return new RestServerException(code, message, msg);
    }

}
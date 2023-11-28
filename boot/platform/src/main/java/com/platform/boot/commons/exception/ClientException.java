package com.platform.boot.commons.exception;

import lombok.Getter;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Getter
public class ClientException extends RestServerException {

    private String serviceId;

    public ClientException(int code, String message, Object msg) {
        super(code, message, msg);
    }

    public static ClientException withMsg(String message, Object msg) {
        return new ClientException(5020, message, msg);
    }

    public ClientException serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

}
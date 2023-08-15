package com.platform.boot.commons.annotation.exception;

import lombok.Getter;

/**
 * This class represents an exception that occurs when a client request is invalid.
 * It extends the RestServerException class.
 * It contains a serviceId field and methods to set and get it.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Getter
public class ClientRequestException extends RestServerException {

    /**
     * -- GETTER --
     * Returns the serviceId field of this exception.
     */
    private String serviceId;

    public ClientRequestException(int code, Object msg) {
        super(code, msg);
    }

    /**
     * Creates a new instance of ClientRequestException with the given code and message.
     *
     * @param code the error code
     * @param msg  the error message
     * @return a new instance of ClientRequestException
     */
    public static ClientRequestException withMsg(int code, Object msg) {
        return new ClientRequestException(code, msg);
    }

    /**
     * Creates a new instance of ClientRequestException with the default error code and the given message.
     *
     * @param msg the error message
     * @return a new instance of ClientRequestException
     */
    public static ClientRequestException withMsg(Object msg) {
        return withMsg(1502, msg);
    }

    /**
     * Sets the serviceId field of this exception and returns this exception.
     *
     * @param serviceId the serviceId to set
     * @return this exception
     */
    public ClientRequestException serviceId(String serviceId) {
        this.setServiceId(serviceId);
        return this;
    }

    /**
     * Sets the serviceId field of this exception.
     *
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
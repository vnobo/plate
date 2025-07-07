package com.plate.boot.commons.utils;

import com.plate.boot.commons.exception.RestServerException;
import org.springframework.lang.Nullable;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">AlexBob</a>)
 */
public enum MethodType {

    DELETE;

    private static final MethodType[] VALUES;

    static {
        VALUES = values();
    }

    public static MethodType value(String statusCode) {
        MethodType status = resolve(statusCode);
        if (status == null) {
            throw RestServerException.withMsg("系统类型转换错误!",
                    new IllegalArgumentException("Code 不能为空!"));
        }
        return status;
    }


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
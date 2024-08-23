package com.plate.boot.commons;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record ErrorResponse(String requestId, String path, Integer code,
                            String message, Object errors, LocalDateTime time) implements Serializable {
    /**
     * 创建一个错误响应对象
     *
     * @param requestId 请求的唯一标识符
     * @param path 请求的路径
     * @param code 错误代码
     * @param message 错误消息
     * @param errors 附加错误信息
     * @return 返回新创建的错误响应对象
     */
    public static ErrorResponse of(String requestId, String path, Integer code, String message, Object errors) {
        return new ErrorResponse(requestId, path, code, message, errors, LocalDateTime.now());
    }
}
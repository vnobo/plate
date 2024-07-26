package com.plate.boot.relational.rsocket;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public enum MessageType implements Serializable {
    /**
     * 命令
     */
    COMMAND,

    /**
     * 未知
     */
    UNKNOWN;
}

package com.plate.boot.relational.rsocket;

import lombok.Data;

import java.io.Serializable;


/**
 * 请求命令的封装类
 * 该类表示一个命令请求，包含请求的类型、命令和内容
 */
@Data
public class CommandRequest implements Serializable {
    /**
     * 请求的类型，使用MessageType枚举表示
     */
    private MessageType type;

    /**
     * 请求的命令，使用字符串表示
     */
    private String command;

    /**
     * 请求的内容，使用字符串表示
     */
    private String content;
}

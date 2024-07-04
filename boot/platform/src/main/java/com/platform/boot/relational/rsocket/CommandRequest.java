package com.platform.boot.relational.rsocket;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
public class CommandRequest implements Serializable {

    private MessageType type;
    private String command;
    private String content;

}

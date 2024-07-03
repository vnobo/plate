package com.platform.boot.relational.rsocket;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@NoArgsConstructor
public class MessageIn implements Serializable {

    private MessageType type;
    private String content;
    private Object data;
    private String from;
    private String to;

    public MessageIn(MessageType type, String content, Object data) {
        this.type = type;
        this.content = content;
        this.data = data;
    }

    public static MessageIn of(MessageType type, String content, Object data) {
        return new MessageIn(type, content, data);
    }
}

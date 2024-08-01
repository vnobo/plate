package com.plate.boot.relational.rsocket;

import com.plate.boot.commons.utils.ContextUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@NoArgsConstructor
public class MessageIn implements Serializable {

    private String code;
    private LocalDateTime time;
    private MessageType type;
    private String content;
    private Object data;
    private String from;
    private String to;

    public MessageIn(MessageType type, String content, Object data) {
        this.type = type;
        this.content = content;
        this.data = data;
        this.data = ContextUtils.nextId();
        this.time = LocalDateTime.now();
    }

    public static MessageIn of(MessageType type, String content, Object data) {
        return new MessageIn(type, content, data);
    }
}

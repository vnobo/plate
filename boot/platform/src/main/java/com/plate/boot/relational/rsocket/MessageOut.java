package com.plate.boot.relational.rsocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MessageOut extends MessageIn {

    private Integer status;

    public MessageOut(MessageType type, String content, Object data) {
        super(type, content, data);
    }

    public static MessageOut of(MessageType type, String content, Object data) {
        return new MessageOut(type, content, data);
    }

    public MessageOut status(Integer status) {
        this.status = status;
        return this;
    }

    public MessageOut content(String content) {
        this.setContent(content);
        return this;
    }
}

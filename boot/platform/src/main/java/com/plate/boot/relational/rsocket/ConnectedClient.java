package com.plate.boot.relational.rsocket;

import lombok.Data;
import org.springframework.messaging.rsocket.RSocketRequester;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 表示一个已连接的客户端，该类封装了客户端的RSocket请求器和连接时间。
 * 实现了Serializable接口以支持对象的序列化。
 */
@Data
public class ConnectedClient implements Serializable {

    /**
     * 客户端的RSocket请求器，用于与服务器进行通信。
     */
    private final RSocketRequester requester;

    /**
     * 客户端连接的确切时间。
     */
    private final LocalDateTime connectedTime;

    /**
     * 构造一个新的 {@link ConnectedClient} 对象。
     *
     * @param requester 客户端的RSocket请求器，不能为空
     */
    public ConnectedClient(RSocketRequester requester) {
        this.requester = requester;
        this.connectedTime = LocalDateTime.now();
    }

}

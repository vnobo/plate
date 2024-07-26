package com.plate.boot.relational.rsocket;

import lombok.Data;
import org.springframework.messaging.rsocket.RSocketRequester;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
public class ConnectedClient implements Serializable {

    private final RSocketRequester requester;
    private final LocalDateTime connectedTime;

    ConnectedClient(RSocketRequester requester) {
        this.requester = requester;
        this.connectedTime = LocalDateTime.now();
    }

}

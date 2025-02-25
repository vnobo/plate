package com.plate.boot.relational.logger;

import com.plate.boot.commons.base.BaseEvent;

import java.util.Optional;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class LoggerEvent extends BaseEvent<LoggerReq> {

    protected LoggerEvent(LoggerReq entity, Kind kind) {
        super(entity, kind);
    }

    public static LoggerEvent insert(LoggerReq entity) {
        return new LoggerEvent(entity, Kind.INSERT);
    }

    public Kind kind() {
        return Optional.ofNullable(super.getKind())
                .orElseThrow(() -> new IllegalArgumentException("Kind is null"));
    }

    public LoggerReq entity() {
        return Optional.ofNullable(super.getEntity())
                .orElseThrow(() -> new IllegalArgumentException("LoggerReq is null"));
    }
}

package com.plate.boot.relational.logger;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerRes extends Logger {

    /**
     * text search rank sort
     */
    @ReadOnlyProperty
    private Double rank;

}

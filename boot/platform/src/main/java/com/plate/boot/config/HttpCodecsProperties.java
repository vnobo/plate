package com.plate.boot.config;

import jakarta.annotation.Nullable;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@ConfigurationProperties("spring.http.codecs")
public class HttpCodecsProperties {

    /**
     * Whether to log form data at DEBUG level, and headers at TRACE level.
     */
    private boolean logRequestDetails;

    /**
     * Limit on the number of bytes that can be buffered whenever the input stream needs
     * to be aggregated. This applies only to the auto-configured WebFlux server and
     * WebClient instances. By default this is not set, in which case individual codec
     * defaults apply. Most codecs are limited to 256K by default.
     */
    private @Nullable DataSize maxInMemorySize;
}

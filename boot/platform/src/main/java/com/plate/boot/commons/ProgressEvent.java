package com.plate.boot.commons;

import com.plate.boot.commons.exception.RestServerException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Progress event class used to encapsulate processing progress related information.
 * Contains processed data amount, message, result object and possible exception information.
 */
@Data
@NoArgsConstructor
public class ProgressEvent implements Serializable {
    private Long processed;
    private Boolean isOk;
    private String message;
    private Object req;
    private Object res;
    private RestServerException error;

    public ProgressEvent(Long processed, Object req) {
        this.processed = processed;
        this.req = req;
        this.isOk = false;
    }

    public static ProgressEvent of(Long processed, Object req) {
        return new ProgressEvent(processed, req);
    }

    public ProgressEvent withMessage(String message) {
        this.message = message;
        this.isOk = true;
        return this;
    }

    public ProgressEvent withResult(String message, Object res) {
        this.message = message;
        this.res = res;
        this.isOk = true;
        return this;
    }

    public ProgressEvent withError(String message, RestServerException error) {
        this.message = message;
        this.error = error;
        this.isOk = false;
        return this;
    }

}

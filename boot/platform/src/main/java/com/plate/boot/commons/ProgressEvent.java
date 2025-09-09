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

    /**
     * Constructs a ProgressEvent with processed count and request object
     *
     * @param processed The number of items processed
     * @param req       The request object associated with this progress event
     */
    public ProgressEvent(Long processed, Object req) {
        this.processed = processed;
        this.req = req;
        this.isOk = false;
    }

    /**
     * Static factory method to create a ProgressEvent instance
     *
     * @param processed The number of items processed
     * @param req       The request object associated with this progress event
     * @return A new ProgressEvent instance
     */
    public static ProgressEvent of(Long processed, Object req) {
        return new ProgressEvent(processed, req);
    }

    /**
     * Sets the message for this progress event and marks it as successful
     *
     * @param message The message to set
     * @return The current ProgressEvent instance for method chaining
     */
    public ProgressEvent withMessage(String message) {
        this.message = message;
        this.isOk = true;
        return this;
    }

    /**
     * Sets the message and result for this progress event and marks it as successful
     *
     * @param message The message to set
     * @param res     The result object to set
     * @return The current ProgressEvent instance for method chaining
     */
    public ProgressEvent withResult(String message, Object res) {
        this.message = message;
        this.res = res;
        this.isOk = true;
        return this;
    }

    /**
     * Sets the message and error for this progress event and marks it as failed
     *
     * @param message The message to set
     * @param error   The RestServerException to set
     * @return The current ProgressEvent instance for method chaining
     */
    public ProgressEvent withError(String message, RestServerException error) {
        this.message = message;
        this.error = error;
        this.isOk = false;
        return this;
    }

}

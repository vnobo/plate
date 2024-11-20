package com.plate.boot.commons.exception;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class QueryException extends RestServerException {

    public QueryException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public static QueryException withError(String message, Throwable throwable) {
        return new QueryException(message, throwable);
    }
}

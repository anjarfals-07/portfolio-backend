package com.anjar.portfolio.exception;

/**
 * Exception untuk error di proses restore database.
 */
public class RestoreException extends RuntimeException {

    public RestoreException(String message) {
        super(message);
    }

    public RestoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
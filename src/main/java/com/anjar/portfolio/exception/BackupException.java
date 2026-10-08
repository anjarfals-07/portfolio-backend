package com.anjar.portfolio.exception;

/**
 * Exception khusus untuk error di proses backup/restore.
 */
public class BackupException extends RuntimeException {

    public BackupException(String message) {
        super(message);
    }

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}
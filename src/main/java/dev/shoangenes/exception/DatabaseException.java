package dev.shoangenes.exception;

/**
 * Custom exception for database-related errors in file repository operations.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
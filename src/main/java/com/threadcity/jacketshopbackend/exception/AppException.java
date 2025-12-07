package com.threadcity.jacketshopbackend.exception;

/**
 * Base exception for the entire application.
 * Contains only message, cause, and errorCode.
 * Does NOT contain HttpStatus (separation of concerns).
 */
public abstract class AppException extends RuntimeException {
    private final String errorCode;

    protected AppException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected AppException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

package com.threadcity.jacketshopbackend.exception;

/**
 * 409 - Resource Conflict
 * Used for: Duplicate email, duplicate unique constraints
 */
public class ResourceConflictException extends AppException {
    public ResourceConflictException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceConflictException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

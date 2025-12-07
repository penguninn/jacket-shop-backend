package com.threadcity.jacketshopbackend.exception;

/**
 * 403 - Authorization Failed
 * Used for: Access denied, insufficient permissions
 */
public class AuthorizationFailedException extends AppException {
    public AuthorizationFailedException(String errorCode, String message) {
        super(errorCode, message);
    }
}

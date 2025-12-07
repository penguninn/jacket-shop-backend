package com.threadcity.jacketshopbackend.exception;

/**
 * 401 - Authentication Failed
 * Used for: Login failed, invalid credentials, token expired
 */
public class AuthenticationFailedException extends AppException {
    public AuthenticationFailedException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AuthenticationFailedException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

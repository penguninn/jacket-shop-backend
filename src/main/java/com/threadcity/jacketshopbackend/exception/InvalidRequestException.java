package com.threadcity.jacketshopbackend.exception;

/**
 * 400 - Invalid Request / Business Rule Violation
 * Used for: Invalid input, logic violations
 */
public class InvalidRequestException extends AppException {
    public InvalidRequestException(String errorCode, String message) {
        super(errorCode, message);
    }
}

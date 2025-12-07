package com.threadcity.jacketshopbackend.exception;

/**
 * 404 - Resource Not Found
 * Used for: User not found, Product not found, etc.
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
}

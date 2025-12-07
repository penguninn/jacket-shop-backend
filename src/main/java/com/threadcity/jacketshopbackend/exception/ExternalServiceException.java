package com.threadcity.jacketshopbackend.exception;

/**
 * 502/503 - External Service Failed
 * Used for: Cloudinary, Payment Gateway, etc.
 */
public class ExternalServiceException extends AppException {
    private final String serviceName;

    public ExternalServiceException(String serviceName, String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}

package com.threadcity.jacketshopbackend.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI BLANK_URI = URI.create("about:blank");

    // ==================================================================================
    // 1. Validation & Bad Requests (400, 422)
    // ==================================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setType(BLANK_URI);
        problem.setTitle("Validation Error");
        problem.setProperty("errorCode", ErrorCodes.VALIDATION_FAILED);

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedRequest(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(BLANK_URI);
        problem.setTitle("Malformed Request");
        problem.setProperty("errorCode", ErrorCodes.MALFORMED_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRequest(InvalidRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), "Invalid Request", ex.getMessage());
    }

    // ==================================================================================
    // 2. Domain Exceptions (404, 409)
    // ==================================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflict(ResourceConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorCode(), "Resource Conflict", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Often caused by unique constraints being violated or foreign key issues
        return buildResponse(HttpStatus.CONFLICT, ErrorCodes.DATABASE_CONSTRAINT_VIOLATION, "Database Conflict",
                "Operation violated database constraints");
    }

    // ==================================================================================
    // 3. Security Exceptions (401, 403)
    // ==================================================================================

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), "Authentication Failed", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleSpringAuthenticationException(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_INVALID_CREDENTIALS, "Authentication Failed",
                ex.getMessage());
    }

    @ExceptionHandler(AuthorizationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthorizationFailed(AuthorizationFailedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getErrorCode(), "Access Denied", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleSpringAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCodes.ACCESS_DENIED, "Access Denied",
                "You do not have permission to access this resource");
    }

    // ==================================================================================
    // 4. External Services (502)
    // ==================================================================================

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetail> handleExternalServiceError(ExternalServiceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setType(BLANK_URI);
        problem.setTitle("External Service Error");
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("service", ex.getServiceName());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    // ==================================================================================
    // 5. Legacy & Catch-All (500)
    // ==================================================================================

    // Create handlers for legacy exceptions to maintain backward compatibility
    // during refactoring
    // Once all services are refactored, these can be removed.

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleLegacyEntityNotFound(EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, "Resource Not Found",
                ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllUncaughtExceptions(Exception ex) {
        String requestId = UUID.randomUUID().toString();
        // Log the exception here with requestId (using slf4j) - assuming logger is
        // meant to be added or handled by aspect
        // log.error("Unhandled exception [ID: {}]", requestId, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        problem.setType(BLANK_URI);
        problem.setTitle("Internal Server Error");
        problem.setProperty("errorCode", ErrorCodes.INTERNAL_ERROR);
        problem.setProperty("requestId", requestId);

        // Don't leak stack trace in production message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    // ==================================================================================
    // Helper Methods
    // ==================================================================================

    private ResponseEntity<ProblemDetail> buildResponse(HttpStatus status, String errorCode, String title,
            String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(BLANK_URI);
        problem.setTitle(title);
        problem.setProperty("errorCode", errorCode);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}

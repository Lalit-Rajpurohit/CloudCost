package com.cloudmonitor.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import software.amazon.awssdk.services.costexplorer.model.CostExplorerException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API errors.
 *
 * Converts exceptions to standardized error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle AWS Cost Explorer API exceptions.
     */
    @ExceptionHandler(CostExplorerException.class)
    public ResponseEntity<Map<String, Object>> handleCostExplorerException(CostExplorerException ex) {
        log.error("AWS Cost Explorer error: {}", ex.awsErrorDetails().errorMessage(), ex);

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "AWS_API_ERROR",
                "Error communicating with AWS Cost Explorer: " + ex.awsErrorDetails().errorMessage()
        );
    }

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errors);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                errors
        );
    }

    /**
     * Handle missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(
            MissingServletRequestParameterException ex) {

        log.warn("Missing parameter: {}", ex.getParameterName());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                "Required parameter '" + ex.getParameterName() + "' is missing"
        );
    }

    /**
     * Handle type mismatch errors (e.g., invalid date format).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getValue());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'"
        );
    }

    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                ex.getMessage()
        );
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later."
        );
    }

    /**
     * Build a standardized error response.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String code, String message) {

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("code", code);
        error.put("message", message);

        return ResponseEntity.status(status).body(error);
    }
}

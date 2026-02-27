package com.carddemo.exception;

import com.carddemo.model.response.CardUpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler
 * Maps COBOL ABEND codes and error conditions to HTTP responses
 *
 * COBOL error handling → HTTP Status mapping:
 * ─────────────────────────────────────────────
 * NOTFND condition     → 404 Not Found
 * DUPREC condition     → 409 Conflict
 * Validation errors    → 400 Bad Request
 * IOERR condition      → 500 Internal Server Error
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles CardNotFoundException
     * Migrated from COBOL NOTFND condition handling
     */
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<CardUpdateResponse> handleCardNotFound(
            CardNotFoundException ex) {
        log.warn("Card not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CardUpdateResponse.builder()
                        .status("FAILURE")
                        .message(ex.getMessage())
                        .returnCode(ex.getReturnCode())
                        .build());
    }

    /**
     * Handles CardUpdateException
     * Migrated from COBOL business rule error handling
     */
    @ExceptionHandler(CardUpdateException.class)
    public ResponseEntity<CardUpdateResponse> handleCardUpdateException(
            CardUpdateException ex) {
        log.warn("Card update error [{}]: {}", ex.getReturnCode(), ex.getMessage());
        HttpStatus status = "0009".equals(ex.getReturnCode())
                ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(CardUpdateResponse.builder()
                        .status("FAILURE")
                        .message(ex.getMessage())
                        .returnCode(ex.getReturnCode())
                        .build());
    }

    /**
     * Handles Bean Validation errors
     * Migrated from COBOL VALIDATE-INPUT-KEY-FIELDS error handling
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CardUpdateResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CardUpdateResponse.builder()
                        .status("FAILURE")
                        .message("Validation failed: " + errors)
                        .returnCode("0006")
                        .build());
    }

    /**
     * Handles unexpected errors
     * Migrated from COBOL ABEND handling
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CardUpdateResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CardUpdateResponse.builder()
                        .status("FAILURE")
                        .message("An unexpected error occurred")
                        .returnCode("9999")
                        .build());
    }
}
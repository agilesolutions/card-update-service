package com.carddemo.exception;

import lombok.Getter;

/**
 * Exception for card update business rule violations
 * Replaces COBOL ABEND codes and error paragraphs
 */
@Getter
public class CardUpdateException extends RuntimeException {
    private final String returnCode;

    public CardUpdateException(String returnCode, String message) {
        super(message);
        this.returnCode = returnCode;
    }

    public CardUpdateException(String returnCode, String message, Throwable cause) {
        super(message, cause);
        this.returnCode = returnCode;
    }
}
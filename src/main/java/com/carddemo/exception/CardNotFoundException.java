package com.carddemo.exception;

import lombok.Getter;

@Getter
public class CardNotFoundException extends RuntimeException {
    private final String returnCode;

    public CardNotFoundException(String message, String returnCode) {
        super(message);
        this.returnCode = returnCode;
    }
}
package com.example.featureflags.exception;

public class FlagNotFoundException extends RuntimeException {
    public FlagNotFoundException(String message) {
        super(message);
    }
}

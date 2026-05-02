package com.example.featureflags.exception;

public class DuplicateFlagException extends RuntimeException {
    public DuplicateFlagException(String message) {
        super(message);
    }
}

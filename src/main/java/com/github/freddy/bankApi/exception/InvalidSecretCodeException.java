package com.github.freddy.bankApi.exception;

public class InvalidSecretCodeException extends RuntimeException {
    public InvalidSecretCodeException(String message) {
        super(message);
    }
}

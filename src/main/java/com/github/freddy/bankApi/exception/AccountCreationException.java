package com.github.freddy.bankApi.exception;

public class AccountCreationException extends RuntimeException {
    public AccountCreationException(String message) {
        super(message);
    }
}
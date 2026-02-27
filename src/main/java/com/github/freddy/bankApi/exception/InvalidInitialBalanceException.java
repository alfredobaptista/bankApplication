package com.github.freddy.bankApi.exception;

public class InvalidInitialBalanceException extends RuntimeException {
    public InvalidInitialBalanceException(String msg) { super(msg); }
}
package com.github.freddy.bankApi.exception;

public class InvalidAccountStatusException extends RuntimeException{
    public InvalidAccountStatusException(String message){
        super(message);
    }
}

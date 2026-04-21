package com.jozedev.bankapp.account.exception;

public class NotActiveException extends RuntimeException {
    public NotActiveException(String message, Object... args) {
        super(String.format(message, args));
    }
}

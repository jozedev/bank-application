package com.jozedev.bankapp.account.exception;

public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message, Object... args) {
        super(String.format(message, args));
    }
}

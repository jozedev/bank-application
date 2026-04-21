package com.jozedev.bankapp.client.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String message, Object... args) {
        super((args != null && args.length > 0) ? String.format(message, args) : message);
    }
}

package com.tinqin.bff.core.exception;

public class EmptyUserCartException extends RuntimeException {

    private static final String MESSAGE = "User's cart is empty.";

    public EmptyUserCartException() {
        super(MESSAGE);
    }
}

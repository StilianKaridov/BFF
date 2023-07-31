package com.tinqin.bff.core.exception;

public class UserExistsException extends RuntimeException {

    private static final String MESSAGE = "User with that email exists.";

    public UserExistsException() {
        super(MESSAGE);
    }
}

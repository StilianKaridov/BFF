package com.tinqin.bff.core.exception;

public class NoSuchUserException extends RuntimeException {

    private static final String MESSAGE = "This user does not exist.";

    public NoSuchUserException() {
        super(MESSAGE);
    }
}

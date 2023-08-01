package com.tinqin.bff.core.exception;

public class NoSuchItemException extends RuntimeException {

    private static final String MESSAGE = "No such item.";

    public NoSuchItemException() {
        super(MESSAGE);
    }
}

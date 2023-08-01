package com.tinqin.bff.core.exception;

public class NotEnoughQuantityException extends RuntimeException {

    private static final String MESSAGE = "Not enough quantity.";

    public NotEnoughQuantityException() {
        super(MESSAGE);
    }
}

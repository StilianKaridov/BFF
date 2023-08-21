package com.tinqin.bff.customannotation.exception;

public class UnsupportedRequestTypeException extends RuntimeException {

    private static final String MESSAGE = "Unsupported request type.";

    public UnsupportedRequestTypeException() {
        super(MESSAGE);
    }
}

package com.tinqin.bff.customannotation.exception;

public class InvalidRequestLineException extends RuntimeException {

    private static final String MESSAGE = "RequestLine must have request method type.";

    public InvalidRequestLineException() {
        super(MESSAGE);
    }
}

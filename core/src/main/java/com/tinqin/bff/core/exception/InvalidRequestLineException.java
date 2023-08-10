package com.tinqin.bff.core.exception;

public class InvalidRequestLineException extends Throwable {

    private static final String MESSAGE = "RequestLine must have request method type.";

    public InvalidRequestLineException() {
        super(MESSAGE);
    }
}

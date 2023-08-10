package com.tinqin.bff.core.exception;

public class RequestMappingMethodNotFound extends Throwable {

    private static final String MESSAGE = "RequestMapping must have request method type.";

    public RequestMappingMethodNotFound() {
        super(MESSAGE);
    }
}

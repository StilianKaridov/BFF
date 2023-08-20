package com.tinqin.bff.customannotation.exception;

public class RequestMappingMethodNotFound extends RuntimeException {

    private static final String MESSAGE = "RequestMapping must have request method type.";

    public RequestMappingMethodNotFound() {
        super(MESSAGE);
    }
}

package com.tinqin.bff.core.exception;

public class NotEnoughQuantityException extends RuntimeException {

    public NotEnoughQuantityException(String message) {
        super(message);
    }
}

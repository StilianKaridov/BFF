package com.tinqin.bff.core.exception;

public class ExistingPhoneNumberException extends RuntimeException {

    private static final String MESSAGE = "User with this phone number already exists.";

    public ExistingPhoneNumberException() {
        super(MESSAGE);
    }
}

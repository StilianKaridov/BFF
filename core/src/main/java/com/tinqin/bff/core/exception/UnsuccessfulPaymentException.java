package com.tinqin.bff.core.exception;

public class UnsuccessfulPaymentException extends RuntimeException {

    public UnsuccessfulPaymentException() {
        super("Payment wasn't successful!");
    }
}

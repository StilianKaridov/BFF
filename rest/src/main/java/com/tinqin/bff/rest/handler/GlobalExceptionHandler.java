package com.tinqin.bff.rest.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<String> handlerConstraintViolationException(ConstraintViolationException ex) {
        StringBuilder sb = new StringBuilder();

        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            sb.append(cv.getMessageTemplate()).append("\n");
        }

        return ResponseEntity.badRequest().body(sb.toString());
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<String> handlerMissingRequestParameterException(MissingServletRequestParameterException ex) {
        String errorMessage = ex.getParameterName() + " is required.";

        return ResponseEntity.badRequest().body(errorMessage);
    }
}

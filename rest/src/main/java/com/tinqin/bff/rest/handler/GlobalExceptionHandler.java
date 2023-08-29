package com.tinqin.bff.rest.handler;

import com.tinqin.bff.core.exception.ExistingPhoneNumberException;
import com.tinqin.bff.core.exception.NoSuchItemException;
import com.tinqin.bff.core.exception.NoSuchUserException;
import com.tinqin.bff.core.exception.NotEnoughQuantityException;
import com.tinqin.bff.core.exception.EmptyUserCartException;
import com.tinqin.bff.core.exception.UnsuccessfulPaymentException;
import com.tinqin.bff.core.exception.UserExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(value = UserExistsException.class)
    public ResponseEntity<String> handlerUserExistsException(UserExistsException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(value = ExistingPhoneNumberException.class)
    public ResponseEntity<String> handlerExistingPhoneNumberException(ExistingPhoneNumberException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<String> handlerBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<String> handlerUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(value = NoSuchUserException.class)
    public ResponseEntity<String> handlerNoSuchUserException(NoSuchUserException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(value = NoSuchItemException.class)
    public ResponseEntity<String> handlerNoSuchItemException(NoSuchItemException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(value = NotEnoughQuantityException.class)
    public ResponseEntity<String> handlerNotEnoughQuantityException(NotEnoughQuantityException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(value = EmptyUserCartException.class)
    public ResponseEntity<String> handlerEmptyUserCartException(EmptyUserCartException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        StringBuilder errors = new StringBuilder();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String message = error.getDefaultMessage();
            errors.append(message).append("\n");
        });

        return ResponseEntity.badRequest().body(errors.toString());
    }

    @ExceptionHandler(value = UnsuccessfulPaymentException.class)
    public ResponseEntity<String> handlerUnsuccessfulPaymentException(UnsuccessfulPaymentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}

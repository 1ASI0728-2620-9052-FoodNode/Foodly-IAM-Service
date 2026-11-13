package com.foodly.iam.interfaces.exceptions;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(
            Exception exception
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            IllegalStateException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<Map<String, String>> handleConflict(
            Exception exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "Email or username already registered"
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleCredentials(
            BadCredentialsException exception
    ) {
        return error(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException exception
    ) {
        return error(
                HttpStatus.FORBIDDEN,
                "Access denied"
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(
            ResponseStatusException exception
    ) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(Map.of(
                        "message",
                        exception.getReason() == null
                                ? "Request failed"
                                : exception.getReason()
                ));
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(Map.of("message", message));
    }
}

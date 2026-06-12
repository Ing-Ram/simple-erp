package com.simpleerp.shared;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates domain and validation exceptions into the shared {@link ApiError} shape. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 for missing entities. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(404, e.getMessage()));
    }

    /** 409 for operations illegal in the current state. */
    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiError> invalidState(InvalidStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(409, e.getMessage()));
    }

    /** 400 for domain validation failures raised outside bean validation. */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> domainValidation(ValidationException e) {
        return ResponseEntity.badRequest().body(ApiError.of(400, e.getMessage()));
    }

    /** 400 with per-field messages for bean validation failures. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
        Map<String, String> fields = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(400, "Validation failed", fields, Instant.now()));
    }
}

package com.simpleerp.shared;

/** Thrown when domain input is invalid beyond what bean validation catches; mapped to HTTP 400. */
public class ValidationException extends RuntimeException {

    /** Creates the exception describing what failed validation. */
    public ValidationException(String message) {
        super(message);
    }
}

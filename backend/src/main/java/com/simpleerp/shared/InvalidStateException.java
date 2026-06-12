package com.simpleerp.shared;

/** Thrown when an operation is not allowed in the entity's current state; mapped to HTTP 409. */
public class InvalidStateException extends RuntimeException {

    /** Creates the exception with an explanation of the illegal transition. */
    public InvalidStateException(String message) {
        super(message);
    }
}

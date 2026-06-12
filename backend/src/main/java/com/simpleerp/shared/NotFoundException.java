package com.simpleerp.shared;

/** Thrown when a requested entity does not exist; mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {

    /** Builds a message like {@code "Invoice 42 not found"}. */
    public NotFoundException(String entity, Long id) {
        super(entity + " " + id + " not found");
    }
}

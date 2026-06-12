package com.simpleerp.shared;

import java.time.Instant;
import java.util.Map;

/** Uniform error payload returned by the global exception handler. */
public record ApiError(int status, String message, Map<String, String> fieldErrors, Instant timestamp) {

    /** Creates an error without field-level details. */
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Map.of(), Instant.now());
    }
}

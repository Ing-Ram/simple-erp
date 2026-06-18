package com.simpleerp.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Credentials submitted to the login endpoint. */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}

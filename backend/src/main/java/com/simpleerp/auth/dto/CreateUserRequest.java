package com.simpleerp.auth.dto;

import com.simpleerp.auth.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Admin payload for creating a user. The password is hashed before storage. */
public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String displayName,
        @NotNull Role role,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password) {
}

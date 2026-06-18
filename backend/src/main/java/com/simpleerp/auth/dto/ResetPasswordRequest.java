package com.simpleerp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin payload for setting a user's new password. */
public record ResetPasswordRequest(
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password) {
}

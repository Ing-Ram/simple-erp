package com.simpleerp.auth.dto;

import com.simpleerp.auth.Role;
import jakarta.validation.constraints.NotNull;

/** Admin payload for changing a user's role. */
public record ChangeRoleRequest(@NotNull Role role) {
}

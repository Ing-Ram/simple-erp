package com.simpleerp.auth.dto;

import com.simpleerp.auth.AppUser;
import com.simpleerp.auth.Role;

/** A user as returned to admins. The password hash is never exposed. */
public record UserResponse(Long id, String username, String displayName, Role role) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole());
    }
}

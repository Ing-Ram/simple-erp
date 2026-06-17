package com.simpleerp.auth.dto;

import com.simpleerp.auth.AppUser;
import com.simpleerp.auth.Role;

/** The current user's identity, for the frontend to restore a session from a stored token. */
public record MeResponse(String username, String displayName, Role role) {

    /** Maps the authenticated user to its response shape. */
    public static MeResponse from(AppUser user) {
        return new MeResponse(user.getUsername(), user.getDisplayName(), user.getRole());
    }
}

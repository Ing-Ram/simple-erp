package com.simpleerp.auth.dto;

import com.simpleerp.auth.Role;

/** The token and the identity behind it, returned on successful login. */
public record LoginResponse(String token, String username, String displayName, Role role) {
}

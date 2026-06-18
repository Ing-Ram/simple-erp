package com.simpleerp.auth;

import com.simpleerp.auth.dto.LoginRequest;
import com.simpleerp.auth.dto.LoginResponse;
import com.simpleerp.auth.dto.MeResponse;
import com.simpleerp.shared.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Authentication endpoints: log in for a token, and read the current identity. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, UserRepository users, JwtService jwt) {
        this.authManager = authManager;
        this.users = users;
        this.jwt = jwt;
    }

    /** Verifies credentials and returns a signed token plus the user's identity. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        AppUser user = users.findByUsername(request.username())
                .orElseThrow(() -> new NotFoundException("User", null));
        return new LoginResponse(jwt.issue(user), user.getUsername(), user.getDisplayName(), user.getRole());
    }

    /** Returns the authenticated user, so the frontend can restore a session from a stored token. */
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        AppUser user = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new NotFoundException("User", null));
        return MeResponse.from(user);
    }
}

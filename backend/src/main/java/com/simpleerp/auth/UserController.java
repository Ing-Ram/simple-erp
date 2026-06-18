package com.simpleerp.auth;

import com.simpleerp.auth.dto.ChangeRoleRequest;
import com.simpleerp.auth.dto.CreateUserRequest;
import com.simpleerp.auth.dto.ResetPasswordRequest;
import com.simpleerp.auth.dto.UserResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only user management. Authorization to ROLE_ADMIN is enforced in SecurityConfig. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    /** Lists all users. */
    @GetMapping
    public List<UserResponse> list() {
        return service.list();
    }

    /** Creates a user and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + user.id())).body(user);
    }

    /** Sets a new password for a user. */
    @PostMapping("/{id}/reset-password")
    public UserResponse resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        return service.resetPassword(id, request.password());
    }

    /** Changes a user's role. */
    @PostMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        return service.changeRole(id, request.role());
    }
}

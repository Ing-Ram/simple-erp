package com.simpleerp.auth;

import com.simpleerp.auth.dto.CreateUserRequest;
import com.simpleerp.auth.dto.UserResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin-facing user management: list accounts, create them, reset passwords, change roles. */
@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /** All users, by username. */
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAll().stream()
                .sorted((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()))
                .map(UserResponse::from)
                .toList();
    }

    /** Creates a user with a hashed password; rejects a duplicate username. */
    public UserResponse create(CreateUserRequest request) {
        if (users.findByUsername(request.username()).isPresent()) {
            throw new InvalidStateException("Username '" + request.username() + "' is already taken");
        }
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setDisplayName(request.displayName());
        user.setRole(request.role());
        user.setPasswordHash(encoder.encode(request.password()));
        return UserResponse.from(users.save(user));
    }

    /** Sets a new (hashed) password for a user. */
    public UserResponse resetPassword(Long id, String newPassword) {
        AppUser user = load(id);
        user.setPasswordHash(encoder.encode(newPassword));
        return UserResponse.from(user);
    }

    /** Changes a user's role. */
    public UserResponse changeRole(Long id, Role role) {
        AppUser user = load(id);
        user.setRole(role);
        return UserResponse.from(user);
    }

    private AppUser load(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }
}

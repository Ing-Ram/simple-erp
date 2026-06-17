package com.simpleerp.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for application users. */
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /** Looks up a user by their unique username, for authentication. */
    Optional<AppUser> findByUsername(String username);
}

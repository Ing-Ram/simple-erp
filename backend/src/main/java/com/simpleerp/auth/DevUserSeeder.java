package com.simpleerp.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a demo login for local use only (never under the prod profile), so there's an account to
 * sign in with. Idempotent — does nothing if the user already exists.
 */
@Component
@Profile("!prod")
public class DevUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);
    private static final String DEMO_USERNAME = "admin";
    private static final String DEMO_PASSWORD = "admin123";

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DevUserSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.findByUsername(DEMO_USERNAME).isPresent()) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(DEMO_USERNAME);
        admin.setPasswordHash(encoder.encode(DEMO_PASSWORD));
        admin.setDisplayName("Demo Admin");
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Seeded demo login — username '{}', password '{}' (dev only)", DEMO_USERNAME, DEMO_PASSWORD);
    }
}

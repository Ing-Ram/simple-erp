package com.simpleerp.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin in production from environment variables, solving the chicken-and-egg
 * of having no account to log in with. Runs only under the prod profile, only when both
 * SIMPLEERP_ADMIN_USERNAME and SIMPLEERP_ADMIN_PASSWORD are set, and only if the user is absent.
 */
@Component
@Profile("prod")
public class ProdAdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdAdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public ProdAdminBootstrap(UserRepository users, PasswordEncoder encoder,
                              @Value("${simpleerp.auth.bootstrap-admin-username:}") String username,
                              @Value("${simpleerp.auth.bootstrap-admin-password:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() || password.isBlank()) {
            log.warn("No bootstrap admin configured (set SIMPLEERP_ADMIN_USERNAME/PASSWORD); "
                    + "no users will exist until one is created.");
            return;
        }
        if (users.findByUsername(username).isPresent()) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setPasswordHash(encoder.encode(password));
        admin.setDisplayName("Administrator");
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Bootstrapped admin user '{}' from the environment", username);
    }
}

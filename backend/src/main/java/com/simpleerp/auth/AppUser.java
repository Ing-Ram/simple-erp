package com.simpleerp.auth;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** An application user. Named AppUser to avoid clashing with Spring Security's User type. */
@Entity
@Table(name = "users")
public class AppUser extends AuditableEntity {

    @Column(unique = true)
    private String username;

    /** BCrypt hash — the plaintext password is never stored. */
    private String passwordHash;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

package com.simpleerp.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Issues and validates the HMAC-signed JWTs used for stateless authentication. */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl;

    public JwtService(
            @Value("${simpleerp.auth.jwt-secret:dev-only-secret-change-me-0123456789-abcdef}") String secret,
            @Value("${simpleerp.auth.jwt-ttl:PT12H}") Duration ttl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.ttl = ttl;
    }

    /** Issues a token whose subject is the username, carrying display name and role as claims. */
    public String issue(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("name", user.getDisplayName())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** Returns the username from a valid token, or throws if the token is invalid or expired. */
    public String usernameFrom(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}

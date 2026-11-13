package com.foodly.iam.infrastructure.security;

import com.foodly.iam.application.ports.ITokenIssuer;
import com.foodly.iam.domain.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenIssuer implements ITokenIssuer {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtTokenIssuer(
            @Value("${foodly.jwt.secret-base64}")
            String secretBase64,
            @Value("${foodly.jwt.expiration-minutes:60}")
            long expirationMinutes
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        if (expirationMinutes < 1 || expirationMinutes > 60) {
            throw new IllegalArgumentException(
                    "JWT expiration must be between 1 and 60 minutes"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public Token issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(
                expirationMinutes * 60
        );

        String jwt = Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new Token(jwt, expiresAt);
    }

    public AuthenticatedUser validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(
                claims.getSubject()
        );

        String role = claims.get("role", String.class);

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT role is missing"
            );
        }

        return new AuthenticatedUser(userId, role);
    }

    public record AuthenticatedUser(
            UUID userId,
            String role
    ) {
    }
}

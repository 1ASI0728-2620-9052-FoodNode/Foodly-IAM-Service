package com.foodly.iam.infrastructure.security;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.model.valueobjects.PhoneNumber;
import com.foodly.iam.domain.model.valueobjects.PasswordHash;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenIssuerTest {

    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(
                    "foodly-test-secret-key-with-32-plus-bytes"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private JwtTokenIssuer issuer;
    private User user;

    @BeforeEach
    void setUp() {
        issuer = new JwtTokenIssuer(TEST_SECRET, 60);

        user = User.create(
                "Juan Perez",
                new Email("juan@foodly.com"),
                new Username("juanfoodly"),
                new PhoneNumber(null),
                new PasswordHash("encrypted-password"),
                Role.GASTRONOMIC_EXPLORER
        );
    }

    @Test
    void shouldIssueValidJwt() {
        var token = issuer.issue(user);

        assertNotNull(token.value());
        assertFalse(token.value().isBlank());
        assertTrue(token.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void shouldValidateJwtClaims() {
        var token = issuer.issue(user);

        var authenticatedUser =
                issuer.validate(token.value());

        assertEquals(
                user.getId(),
                authenticatedUser.userId()
        );

        assertEquals(
                "GASTRONOMIC_EXPLORER",
                authenticatedUser.role()
        );
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey() {
        String otherSecret = Base64.getEncoder()
                .encodeToString(
                        "different-test-secret-key-with-32-bytes"
                                .getBytes(StandardCharsets.UTF_8)
                );

        JwtTokenIssuer otherIssuer =
                new JwtTokenIssuer(otherSecret, 60);

        String foreignToken =
                otherIssuer.issue(user).value();

        assertThrows(
                JwtException.class,
                () -> issuer.validate(foreignToken)
        );
    }

    @Test
    void shouldRejectExpiredJwt() {
        byte[] keyBytes =
                Base64.getDecoder().decode(TEST_SECRET);

        var signingKey =
                Keys.hmacShaKeyFor(keyBytes);

        Instant now = Instant.now();

        String expiredToken = Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now.minusSeconds(180)))
                .expiration(Date.from(now.minusSeconds(120)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> issuer.validate(expiredToken)
        );
    }
}

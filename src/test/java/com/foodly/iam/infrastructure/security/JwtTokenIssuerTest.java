package com.foodly.iam.infrastructure.security;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.model.valueobjects.PhoneNumber;
import com.foodly.iam.domain.model.valueobjects.PasswordHash;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenIssuerTest {

    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(
                    "foodly-test-secret-key-with-32-plus-bytes"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final String ISSUER = "foodly-iam";

    private static final List<String> AUDIENCES = List.of(
            "foodly-iam",
            "foodly-business",
            "foodly-radar",
            "foodly-messaging",
            "foodly-reservation",
            "foodly-community"
    );

    private static final String AUDIENCES_CSV =
            String.join(",", AUDIENCES);

    private JwtTokenIssuer issuer;
    private User user;

    @BeforeEach
    void setUp() {
        issuer = new JwtTokenIssuer(TEST_SECRET, 60, ISSUER, AUDIENCES_CSV);

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
                new JwtTokenIssuer(otherSecret, 60, ISSUER, AUDIENCES_CSV);

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
                .issuer(ISSUER)
                .audience().add(AUDIENCES).and()
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

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(TEST_SECRET)
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String tokenWith(String issuerClaim, List<String> audienceClaim) {
        Instant now = Instant.now();

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)));

        if (issuerClaim != null) {
            builder.issuer(issuerClaim);
        }

        if (audienceClaim != null) {
            builder.audience().add(audienceClaim).and();
        }

        return builder.signWith(signingKey(), Jwts.SIG.HS256).compact();
    }

    @Test
    void shouldIncludeIssuerAndAudiencesInIssuedToken() {
        var token = issuer.issue(user);

        Claims claims = parseClaims(token.value());

        assertEquals("foodly-iam", claims.getIssuer());
        assertEquals(
                AUDIENCES,
                List.copyOf(claims.getAudience())
        );
        assertEquals(
                user.getId().toString(),
                claims.getSubject()
        );
        assertEquals(
                "GASTRONOMIC_EXPLORER",
                claims.get("role", String.class)
        );
        assertNotNull(claims.getIssuedAt());
        assertEquals(
                3600,
                (claims.getExpiration().getTime()
                        - claims.getIssuedAt().getTime()) / 1000
        );
    }

    @Test
    void shouldSignIssuedTokenWithHs256() {
        String token = issuer.issue(user).value();

        String header = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                StandardCharsets.UTF_8
        );

        assertTrue(header.contains("\"alg\":\"HS256\""));
    }

    @Test
    void shouldRejectTokenWithWrongIssuer() {
        String token = tokenWith("someone-else", AUDIENCES);

        assertThrows(
                IncorrectClaimException.class,
                () -> issuer.validate(token)
        );
    }

    @Test
    void shouldRejectTokenWithoutIssuer() {
        String token = tokenWith(null, AUDIENCES);

        assertThrows(
                MissingClaimException.class,
                () -> issuer.validate(token)
        );
    }

    @Test
    void shouldRejectTokenWithoutAudience() {
        String token = tokenWith(ISSUER, null);

        assertThrows(
                MissingClaimException.class,
                () -> issuer.validate(token)
        );
    }

    @Test
    void shouldRejectTokenWhoseAudienceExcludesIam() {
        String token = tokenWith(ISSUER, List.of("foodly-business"));

        assertThrows(
                IncorrectClaimException.class,
                () -> issuer.validate(token)
        );
    }

    @Test
    void shouldAcceptTokenWhoseAudienceIncludesIamAmongOthers() {
        String token = tokenWith(
                ISSUER,
                List.of("foodly-business", "foodly-iam")
        );

        assertEquals(
                user.getId(),
                issuer.validate(token).userId()
        );
    }

    @Test
    void shouldAcceptTokenExpiredWithinClockSkew() {
        Instant now = Instant.now();

        String token = Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCES).and()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now.minusSeconds(3630)))
                .expiration(Date.from(now.minusSeconds(30)))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();

        assertEquals(
                user.getId(),
                issuer.validate(token).userId()
        );
    }

    @Test
    void shouldRejectTokenExpiredBeyondClockSkew() {
        Instant now = Instant.now();

        String token = Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCES).and()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now.minusSeconds(3720)))
                .expiration(Date.from(now.minusSeconds(120)))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> issuer.validate(token)
        );
    }

    @Test
    void shouldFailFastWhenAudiencesDoNotIncludeIam() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtTokenIssuer(
                        TEST_SECRET, 60, ISSUER, "foodly-business"
                )
        );
    }

    @Test
    void shouldFailFastWhenIssuerIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtTokenIssuer(
                        TEST_SECRET, 60, " ", AUDIENCES_CSV
                )
        );
    }
}

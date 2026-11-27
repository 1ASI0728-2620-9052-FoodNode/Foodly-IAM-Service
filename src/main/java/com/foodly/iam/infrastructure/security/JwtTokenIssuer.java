package com.foodly.iam.infrastructure.security;

import com.foodly.iam.application.ports.ITokenIssuer;
import com.foodly.iam.domain.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtTokenIssuer implements ITokenIssuer {

    private static final String DEFAULT_ISSUER = "foodly-iam";
    private static final String DEFAULT_AUDIENCES =
            "foodly-iam,foodly-business,foodly-radar,foodly-messaging,foodly-reservation,foodly-community";

    private final SecretKey signingKey;
    private final long expirationMinutes;
    private final String issuer;
    private final List<String> audiences;

    public JwtTokenIssuer(String secretBase64, long expirationMinutes) {
        this(secretBase64, expirationMinutes, DEFAULT_ISSUER, DEFAULT_AUDIENCES);
    }

    @Autowired
    public JwtTokenIssuer(
            @Value("${foodly.jwt.secret-base64}")
            String secretBase64,
            @Value("${foodly.jwt.expiration-minutes:60}")
            long expirationMinutes,
            @Value("${foodly.jwt.issuer:" + DEFAULT_ISSUER + "}")
            String issuer,
            @Value("${foodly.jwt.audiences:" + DEFAULT_AUDIENCES + "}")
            String audiencesCsv
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

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer must not be blank");
        }

        List<String> parsedAudiences = audiencesCsv == null
                ? List.of()
                : Arrays.stream(audiencesCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .toList();

        if (parsedAudiences.isEmpty()) {
            throw new IllegalArgumentException("JWT audiences must not be empty");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer.trim();
        this.audiences = parsedAudiences;
    }

    @Override
    public Token issue(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(
                expirationMinutes * 60
        );

        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt));

        var audBuilder = builder.audience();
        audiences.forEach(audBuilder::add);
        audBuilder.and();

        String jwt = builder
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new Token(jwt, expiresAt);
    }

    public AuthenticatedUser validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Set<String> tokenAudiences = claims.getAudience();
        if (tokenAudiences == null || !tokenAudiences.contains(issuer)) {
            throw new IllegalArgumentException("JWT audience is missing required target");
        }

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

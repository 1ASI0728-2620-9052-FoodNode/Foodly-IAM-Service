package com.foodly.iam.interfaces.resources;

import com.foodly.iam.application.handlers.AuthenticationResult;
import com.foodly.iam.domain.model.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record AuthResponseResource(
        String token,
        String tokenType,
        Instant expiresAt,
        UUID userId,
        String name,
        Role role
) {

    public static AuthResponseResource from(
            AuthenticationResult result
    ) {
        return new AuthResponseResource(
                result.token().value(),
                "Bearer",
                result.token().expiresAt(),
                result.user().getId(),
                result.user().getName(),
                result.user().getRole()
        );
    }
}

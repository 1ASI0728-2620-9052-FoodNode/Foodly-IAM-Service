
package com.foodly.iam.domain.model.valueobjects;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash is required"
            );
        }
    }
}

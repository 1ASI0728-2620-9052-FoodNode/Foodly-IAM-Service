
package com.foodly.iam.domain.model.valueobjects;

public record Username(String value) {

    public Username {
        if (value == null ||
                !value.matches("^[a-zA-Z0-9_]{3,30}$")) {
            throw new IllegalArgumentException(
                    "Username must contain 3-30 letters, numbers or underscores"
            );
        }
    }
}

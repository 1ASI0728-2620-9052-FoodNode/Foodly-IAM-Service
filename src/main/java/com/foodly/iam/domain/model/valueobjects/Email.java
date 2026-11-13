
package com.foodly.iam.domain.model.valueobjects;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {
        if (value == null) {
            throw new IllegalArgumentException("Email is required");
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(value).matches()
                || value.length() > 254) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

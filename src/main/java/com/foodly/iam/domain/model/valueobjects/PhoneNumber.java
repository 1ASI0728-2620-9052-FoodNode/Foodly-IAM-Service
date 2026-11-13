
package com.foodly.iam.domain.model.valueobjects;

public record PhoneNumber(String value) {

    public PhoneNumber {
        if (value != null) {
            value = value.trim();

            if (value.isEmpty()) {
                value = null;
            } else if (!value.matches("^\\+?[0-9]{9,15}$")) {
                throw new IllegalArgumentException(
                        "Invalid phone number"
                );
            }
        }
    }
}

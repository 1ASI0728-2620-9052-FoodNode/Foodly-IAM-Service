
package com.foodly.iam.application.ports;

public interface IPasswordHasher {

    String hash(String rawPassword);

    boolean matches(
            String rawPassword,
            String storedHash
    );
}

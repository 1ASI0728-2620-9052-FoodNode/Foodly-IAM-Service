package com.foodly.iam.infrastructure.security;

import com.foodly.iam.application.ports.IPasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements IPasswordHasher {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(
            String rawPassword,
            String storedHash
    ) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        return encoder.matches(rawPassword, storedHash);
    }
}

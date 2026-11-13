
package com.foodly.iam.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordHasherTest {

    private BCryptPasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new BCryptPasswordHasher();
    }

    @Test
    void shouldHashPassword() {
        String rawPassword = "FoodlyTest123!";

        String hash = passwordHasher.hash(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void shouldVerifyCorrectPassword() {
        String rawPassword = "FoodlyTest123!";
        String hash = passwordHasher.hash(rawPassword);

        assertTrue(
                passwordHasher.matches(rawPassword, hash)
        );
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String hash = passwordHasher.hash("FoodlyTest123!");

        assertFalse(
                passwordHasher.matches("WrongPassword!", hash)
        );
    }
}

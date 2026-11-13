
package com.foodly.iam.domain.model;

import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.model.valueobjects.PhoneNumber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsTest {

    @Test
    void shouldNormalizeEmail() {
        Email email = new Email("  JUAN@FOODLY.COM  ");

        assertEquals("juan@foodly.com", email.value());
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Email("correo-invalido")
        );
    }

    @Test
    void shouldAcceptValidUsername() {
        Username username = new Username("juan_123");

        assertEquals("juan_123", username.value());
    }

    @Test
    void shouldRejectShortUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Username("ab")
        );
    }

    @Test
    void shouldAllowOptionalPhone() {
        PhoneNumber phone = new PhoneNumber(null);

        assertNull(phone.value());
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PhoneNumber("abc123")
        );
    }
}

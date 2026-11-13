
package com.foodly.iam.domain.model;

import com.foodly.iam.domain.model.enums.AccountStatus;
import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.PasswordHash;
import com.foodly.iam.domain.model.valueobjects.PhoneNumber;
import com.foodly.iam.domain.model.valueobjects.Username;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User createUser() {
        return User.create(
                "Juan Perez",
                new Email("juan@foodly.com"),
                new Username("juanfoodly"),
                new PhoneNumber("987654321"),
                new PasswordHash("hashed-password"),
                Role.GASTRONOMIC_EXPLORER
        );
    }

    @Test
    void shouldCreateActiveUser() {
        User user = createUser();

        assertNotNull(user.getId());
        assertEquals(AccountStatus.ACTIVE, user.getStatus());
        assertTrue(user.isActive());
    }

    @Test
    void shouldDeactivateUser() {
        User user = createUser();

        user.deactivate();

        assertFalse(user.isActive());
        assertEquals(
                AccountStatus.DEACTIVATED,
                user.getStatus()
        );
    }

    @Test
    void shouldChangeUserRole() {
        User user = createUser();

        user.changeRole(Role.HUARIQUE_OWNER);

        assertEquals(
                Role.HUARIQUE_OWNER,
                user.getRole()
        );
    }

    @Test
    void shouldRejectEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> User.create(
                        "",
                        new Email("juan@foodly.com"),
                        new Username("juanfoodly"),
                        new PhoneNumber(null),
                        new PasswordHash("hashed-password"),
                        Role.GASTRONOMIC_EXPLORER
                )
        );
    }
}

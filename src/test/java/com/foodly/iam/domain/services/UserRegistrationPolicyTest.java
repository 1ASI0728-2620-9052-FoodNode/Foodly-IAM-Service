
package com.foodly.iam.domain.services;

import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRegistrationPolicyTest {

    private IUserRepository repository;
    private UserRegistrationPolicy policy;

    @BeforeEach
    void setUp() {
        repository = mock(IUserRepository.class);
        policy = new UserRegistrationPolicy(repository);
    }

    @Test
    void shouldAssignDefaultRole() {
        Role role = policy.verify(
                new Email("juan@foodly.com"),
                new Username("juanfoodly"),
                null
        );

        assertEquals(Role.GASTRONOMIC_EXPLORER, role);
    }

    @Test
    void shouldRespectRequestedRole() {
        Role role = policy.verify(
                new Email("dueno@foodly.com"),
                new Username("duenofoodly"),
                Role.HUARIQUE_OWNER
        );

        assertEquals(Role.HUARIQUE_OWNER, role);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(repository.existsByEmail("juan@foodly.com"))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> policy.verify(
                        new Email("juan@foodly.com"),
                        new Username("juanfoodly"),
                        null
                )
        );
    }

    @Test
    void shouldRejectDuplicateUsername() {
        when(repository.existsByUsername("juanfoodly"))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> policy.verify(
                        new Email("juan@foodly.com"),
                        new Username("juanfoodly"),
                        null
                )
        );
    }
}

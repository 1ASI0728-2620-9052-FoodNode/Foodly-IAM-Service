package com.foodly.iam.application.handlers;

import com.foodly.iam.application.commands.RegisterUserCommand;
import com.foodly.iam.application.ports.IPasswordHasher;
import com.foodly.iam.application.ports.ITokenIssuer;
import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegisterUserCommandHandlerTest {

    private IUserRepository repository;
    private IPasswordHasher passwordHasher;
    private ITokenIssuer tokenIssuer;
    private RegisterUserCommandHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(IUserRepository.class);
        passwordHasher = mock(IPasswordHasher.class);
        tokenIssuer = mock(ITokenIssuer.class);

        handler = new RegisterUserCommandHandler(
                repository,
                passwordHasher,
                tokenIssuer
        );

        when(passwordHasher.hash(anyString()))
                .thenReturn("encrypted-password");

        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(tokenIssuer.issue(any(User.class)))
                .thenReturn(new ITokenIssuer.Token(
                        "test-jwt",
                        Instant.now().plusSeconds(3600)
                ));
    }

    private RegisterUserCommand command(Role role) {
        return new RegisterUserCommand(
                "Juan Perez",
                "juan@foodly.com",
                "juanfoodly",
                "987654321",
                "Password123!",
                role
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        AuthenticationResult result = handler.handle(
                command(Role.GASTRONOMIC_EXPLORER)
        );

        assertNotNull(result.user().getId());
        assertTrue(result.user().isActive());
        assertEquals("test-jwt", result.token().value());

        verify(repository).save(any(User.class));
        verify(tokenIssuer).issue(any(User.class));
    }

    @Test
    void shouldAssignDefaultRole() {
        AuthenticationResult result = handler.handle(
                command(null)
        );

        assertEquals(
                Role.GASTRONOMIC_EXPLORER,
                result.user().getRole()
        );
    }

    @Test
    void shouldStoreHashedPassword() {
        handler.handle(command(Role.GASTRONOMIC_EXPLORER));

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(repository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals(
                "encrypted-password",
                savedUser.getPasswordHash().value()
        );

        verify(passwordHasher).hash("Password123!");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(repository.existsByEmail("juan@foodly.com"))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(command(null))
        );

        verify(repository, never()).save(any(User.class));
        verify(tokenIssuer, never()).issue(any(User.class));
    }
}

package com.foodly.iam.application.handlers;

import com.foodly.iam.application.commands.AuthenticateUserCommand;
import com.foodly.iam.application.ports.IPasswordHasher;
import com.foodly.iam.application.ports.ITokenIssuer;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.model.valueobjects.PhoneNumber;
import com.foodly.iam.domain.model.valueobjects.PasswordHash;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthenticateUserCommandHandlerTest {

    private IUserRepository repository;
    private IPasswordHasher passwordHasher;
    private ITokenIssuer tokenIssuer;
    private AuthenticateUserCommandHandler handler;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(IUserRepository.class);
        passwordHasher = mock(IPasswordHasher.class);
        tokenIssuer = mock(ITokenIssuer.class);

        handler = new AuthenticateUserCommandHandler(
                repository,
                passwordHasher,
                tokenIssuer
        );

        user = User.create(
                "Juan Perez",
                new Email("juan@foodly.com"),
                new Username("juanfoodly"),
                new PhoneNumber(null),
                new PasswordHash("encrypted-password"),
                Role.GASTRONOMIC_EXPLORER
        );

        when(repository.findByEmail("juan@foodly.com"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void shouldAuthenticateWithValidCredentials() {
        when(passwordHasher.matches(
                "Password123!",
                "encrypted-password"
        )).thenReturn(true);

        when(tokenIssuer.issue(user))
                .thenReturn(new ITokenIssuer.Token(
                        "valid-jwt",
                        Instant.now().plusSeconds(3600)
                ));

        AuthenticationResult result = handler.handle(
                new AuthenticateUserCommand(
                        "juan@foodly.com",
                        "Password123!"
                )
        );

        assertEquals(user.getId(), result.user().getId());
        assertEquals("valid-jwt", result.token().value());
    }

    @Test
    void shouldRejectIncorrectPassword() {
        when(passwordHasher.matches(
                anyString(),
                anyString()
        )).thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> handler.handle(
                        new AuthenticateUserCommand(
                                "juan@foodly.com",
                                "IncorrectPassword!"
                        )
                )
        );

        verify(tokenIssuer, never()).issue(any(User.class));
    }

    @Test
    void shouldRejectUnknownUser() {
        when(repository.findByEmail("unknown@foodly.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> handler.handle(
                        new AuthenticateUserCommand(
                                "unknown@foodly.com",
                                "Password123!"
                        )
                )
        );

        verify(tokenIssuer, never()).issue(any(User.class));
    }

    @Test
    void shouldRejectDeactivatedAccount() {
        user.deactivate();

        when(passwordHasher.matches(
                "Password123!",
                "encrypted-password"
        )).thenReturn(true);

        assertThrows(
                AccessDeniedException.class,
                () -> handler.handle(
                        new AuthenticateUserCommand(
                                "juan@foodly.com",
                                "Password123!"
                        )
                )
        );

        verify(tokenIssuer, never()).issue(any(User.class));
    }
}

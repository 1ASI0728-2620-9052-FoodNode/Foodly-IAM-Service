
package com.foodly.iam.application.handlers;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetUserProfileQueryHandlerTest {

    private IUserRepository repository;
    private GetUserProfileQueryHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(IUserRepository.class);
        handler = new GetUserProfileQueryHandler(repository);
    }

    @Test
    void shouldReturnActiveUser() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));
        when(user.isActive()).thenReturn(true);

        User result = handler.handle(userId);

        assertSame(user, result);
        verify(repository).findById(userId);
    }

    @Test
    void shouldRejectUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> handler.handle(userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldRejectDeactivatedUser() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));
        when(user.isActive()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> handler.handle(userId)
        );
    }
}

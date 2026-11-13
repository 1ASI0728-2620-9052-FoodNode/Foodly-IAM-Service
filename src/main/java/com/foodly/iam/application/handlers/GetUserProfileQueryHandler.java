package com.foodly.iam.application.handlers;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class GetUserProfileQueryHandler {

    private final IUserRepository users;

    public GetUserProfileQueryHandler(
            IUserRepository users
    ) {
        this.users = users;
    }

    public User handle(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        if (!user.isActive()) {
            throw new AccessDeniedException(
                    "Account deactivated"
            );
        }

        return user;
    }
}


package com.foodly.iam.application.handlers;

import com.foodly.iam.application.commands.AuthenticateUserCommand;
import com.foodly.iam.application.ports.IPasswordHasher;
import com.foodly.iam.application.ports.ITokenIssuer;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthenticateUserCommandHandler {

    private final IUserRepository users;
    private final IPasswordHasher passwords;
    private final ITokenIssuer tokens;

    public AuthenticateUserCommandHandler(
            IUserRepository users,
            IPasswordHasher passwords,
            ITokenIssuer tokens
    ) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public AuthenticationResult handle(
            AuthenticateUserCommand command
    ) {

        if (command.email() == null
                || command.password() == null) {

            throw new BadCredentialsException(
                    "Invalid credentials"
            );
        }

        String email = command.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        var user = users.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid credentials"
                        )
                );

        boolean passwordMatches = passwords.matches(
                command.password(),
                user.getPasswordHash().value()
        );

        if (!passwordMatches) {
            throw new BadCredentialsException(
                    "Invalid credentials"
            );
        }

        if (!user.isActive()) {
            throw new AccessDeniedException(
                    "Account deactivated"
            );
        }

        var token = tokens.issue(user);

        return new AuthenticationResult(user, token);
    }
}

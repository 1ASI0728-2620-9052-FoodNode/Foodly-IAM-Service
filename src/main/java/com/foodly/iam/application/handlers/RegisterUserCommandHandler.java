
package com.foodly.iam.application.handlers;

import com.foodly.iam.application.commands.RegisterUserCommand;
import com.foodly.iam.application.ports.IPasswordHasher;
import com.foodly.iam.application.ports.ITokenIssuer;
import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.model.valueobjects.PhoneNumber;
import com.foodly.iam.domain.model.valueobjects.PasswordHash;
import com.foodly.iam.domain.repositories.IUserRepository;
import com.foodly.iam.domain.services.UserRegistrationPolicy;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserCommandHandler {

    private final IUserRepository users;
    private final IPasswordHasher passwords;
    private final ITokenIssuer tokens;

    public RegisterUserCommandHandler(
            IUserRepository users,
            IPasswordHasher passwords,
            ITokenIssuer tokens
    ) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @Transactional
    public AuthenticationResult handle(RegisterUserCommand command) {

        Email email = new Email(command.email());
        Username username = new Username(command.username());

        UserRegistrationPolicy policy =
                new UserRegistrationPolicy(users);

        var role = policy.verify(
                email,
                username,
                command.role()
        );

        if (command.password() == null
                || command.password().length() < 8
                || command.password().length() > 72) {

            throw new IllegalArgumentException(
                    "Password length must be between 8 and 72 characters"
            );
        }

        String encodedPassword =
                passwords.hash(command.password());

        User user = User.create(
                command.name(),
                email,
                username,
                new PhoneNumber(command.phone()),
                new PasswordHash(encodedPassword),
                role
        );

        try {
            user = users.save(user);

        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "Email or username already registered",
                    exception
            );
        }

        var token = tokens.issue(user);

        return new AuthenticationResult(user, token);
    }
}

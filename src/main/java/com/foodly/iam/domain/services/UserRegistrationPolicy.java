
package com.foodly.iam.domain.services;

import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.Email;
import com.foodly.iam.domain.model.valueobjects.Username;
import com.foodly.iam.domain.repositories.IUserRepository;

public class UserRegistrationPolicy {

    private final IUserRepository users;

    public UserRegistrationPolicy(IUserRepository users) {
        this.users = users;
    }

    public Role verify(
            Email email,
            Username username,
            Role requestedRole
    ) {
        if (users.existsByEmail(email.value())) {
            throw new IllegalStateException(
                    "Email already registered"
            );
        }

        if (users.existsByUsername(username.value())) {
            throw new IllegalStateException(
                    "Username already registered"
            );
        }

        return requestedRole != null
                ? requestedRole
                : Role.GASTRONOMIC_EXPLORER;
    }
}

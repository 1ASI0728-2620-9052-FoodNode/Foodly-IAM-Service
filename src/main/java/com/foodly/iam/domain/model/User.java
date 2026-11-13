
package com.foodly.iam.domain.model;

import com.foodly.iam.domain.model.enums.AccountStatus;
import com.foodly.iam.domain.model.enums.Role;
import com.foodly.iam.domain.model.valueobjects.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final UUID id;
    private String name;
    private final Email email;
    private final Username username;
    private PhoneNumber phone;
    private final PasswordHash passwordHash;
    private Role role;
    private AccountStatus status;
    private final Instant createdAt;

    public User(
            UUID id,
            String name,
            Email email,
            Username username,
            PhoneNumber phone,
            PasswordHash passwordHash,
            Role role,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.email = Objects.requireNonNull(email);
        this.username = Objects.requireNonNull(username);
        this.phone = Objects.requireNonNull(phone);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static User create(
            String name,
            Email email,
            Username username,
            PhoneNumber phone,
            PasswordHash passwordHash,
            Role role
    ) {
        return new User(
                UUID.randomUUID(),
                name,
                email,
                username,
                phone,
                passwordHash,
                role,
                AccountStatus.ACTIVE,
                Instant.now()
        );
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name is required"
            );
        }
        return name.trim();
    }

    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole);
    }

    public void updateProfile(String name, PhoneNumber phone) {
        this.name = requireName(name);
        this.phone = Objects.requireNonNull(phone);
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public Email getEmail() { return email; }

    public Username getUsername() { return username; }

    public PhoneNumber getPhone() { return phone; }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() { return role; }

    public AccountStatus getStatus() { return status; }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

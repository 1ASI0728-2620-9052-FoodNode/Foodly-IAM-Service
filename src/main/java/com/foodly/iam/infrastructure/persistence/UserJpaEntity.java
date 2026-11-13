package com.foodly.iam.infrastructure.persistence;

import com.foodly.iam.domain.model.enums.AccountStatus;
import com.foodly.iam.domain.model.enums.Role;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(length = 16)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(
            UUID id,
            String name,
            String email,
            String username,
            String phone,
            String passwordHash,
            Role role,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getUsername() { return username; }

    public String getPhone() { return phone; }

    public String getPasswordHash() { return passwordHash; }

    public Role getRole() { return role; }

    public AccountStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }
}

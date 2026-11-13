package com.foodly.iam.infrastructure.persistence;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.valueobjects.*;
import com.foodly.iam.domain.repositories.IUserRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements IUserRepository {

    private final UserJpaRepository repository;

    public UserRepositoryAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getEmail().value(),
                user.getUsername().value(),
                user.getPhone().value(),
                user.getPasswordHash().value(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                new Username(entity.getUsername()),
                new PhoneNumber(entity.getPhone()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}

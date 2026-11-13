
package com.foodly.iam.domain.repositories;

import com.foodly.iam.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}

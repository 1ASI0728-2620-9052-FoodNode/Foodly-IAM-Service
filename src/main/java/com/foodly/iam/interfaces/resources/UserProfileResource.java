package com.foodly.iam.interfaces.resources;

import com.foodly.iam.domain.model.User;
import com.foodly.iam.domain.model.enums.Role;

import java.util.UUID;

public record UserProfileResource(
        UUID id,
        String name,
        String email,
        String username,
        String phone,
        Role role
) {

    public static UserProfileResource from(User user) {
        return new UserProfileResource(
                user.getId(),
                user.getName(),
                user.getEmail().value(),
                user.getUsername().value(),
                user.getPhone().value(),
                user.getRole()
        );
    }
}

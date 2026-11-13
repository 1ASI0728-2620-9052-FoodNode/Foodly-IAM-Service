
package com.foodly.iam.application.commands;

import com.foodly.iam.domain.model.enums.Role;

public record RegisterUserCommand(
        String name,
        String email,
        String username,
        String phone,
        String password,
        Role role
) {}

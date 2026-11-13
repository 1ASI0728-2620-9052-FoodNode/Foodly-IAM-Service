
package com.foodly.iam.application.commands;

public record AuthenticateUserCommand(
        String email,
        String password
) {}

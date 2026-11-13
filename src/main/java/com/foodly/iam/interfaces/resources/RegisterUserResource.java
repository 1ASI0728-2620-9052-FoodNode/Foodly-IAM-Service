package com.foodly.iam.interfaces.resources;

import com.foodly.iam.domain.model.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserResource(

        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Username is required")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]{3,30}$",
                message = "Username must contain 3-30 letters, numbers or underscores"
        )
        String username,

        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "Invalid phone number"
        )
        String phone,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must contain 8-72 characters"
        )
        String password,

        Role role
) {
}

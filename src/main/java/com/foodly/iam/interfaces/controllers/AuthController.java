package com.foodly.iam.interfaces.controllers;

import com.foodly.iam.application.commands.AuthenticateUserCommand;
import com.foodly.iam.application.commands.RegisterUserCommand;

import com.foodly.iam.application.handlers.AuthenticateUserCommandHandler;
import com.foodly.iam.application.handlers.RegisterUserCommandHandler;

import com.foodly.iam.interfaces.resources.AuthResponseResource;
import com.foodly.iam.interfaces.resources.LoginResource;
import com.foodly.iam.interfaces.resources.RegisterUserResource;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/iam/auth")
public class AuthController {

    private final RegisterUserCommandHandler registerHandler;
    private final AuthenticateUserCommandHandler authenticateHandler;

    public AuthController(
            RegisterUserCommandHandler registerHandler,
            AuthenticateUserCommandHandler authenticateHandler
    ) {
        this.registerHandler = registerHandler;
        this.authenticateHandler = authenticateHandler;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseResource> register(
            @Valid @RequestBody RegisterUserResource request
    ) {
        var command = new RegisterUserCommand(
                request.name(),
                request.email(),
                request.username(),
                request.phone(),
                request.password(),
                request.role()
        );

        var result = registerHandler.handle(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthResponseResource.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseResource> login(
            @Valid @RequestBody LoginResource request
    ) {
        var command = new AuthenticateUserCommand(
                request.email(),
                request.password()
        );

        var result = authenticateHandler.handle(command);

        return ResponseEntity.ok(
                AuthResponseResource.from(result)
        );
    }
}

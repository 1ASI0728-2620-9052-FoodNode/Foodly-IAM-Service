package com.foodly.iam.interfaces.controllers;

import com.foodly.iam.application.handlers.GetUserProfileQueryHandler;
import com.foodly.iam.interfaces.resources.UserProfileResource;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/iam/users")
public class UserController {

    private final GetUserProfileQueryHandler profileHandler;

    public UserController(
            GetUserProfileQueryHandler profileHandler
    ) {
        this.profileHandler = profileHandler;
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResource> getMyProfile(
            @AuthenticationPrincipal String userId
    ) {
        UUID id = UUID.fromString(userId);

        var user = profileHandler.handle(id);

        return ResponseEntity.ok(
                UserProfileResource.from(user)
        );
    }
}

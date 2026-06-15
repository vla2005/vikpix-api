package com.vikpix.api.auth.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.request.ConfirmPasswordRequest;
import com.vikpix.api.auth.dto.response.ConfirmPasswordResponse;
import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.users.entities.User;

@Service
public class ConfirmPasswordService {
    private final CurrentUserService currentUserService;
    private final KeycloakAdminClient keycloakAdminClient;

    public ConfirmPasswordService(
        CurrentUserService currentUserService,
        KeycloakAdminClient keycloakAdminClient
    ) {
        this.currentUserService = currentUserService;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    public ConfirmPasswordResponse execute(Authentication authentication, ConfirmPasswordRequest request) {
        User user = currentUserService.getAuthenticatedUser(authentication);
        keycloakAdminClient.authenticateUser(user.getEmail(), request.password(), false);
        return new ConfirmPasswordResponse(true);
    }
}
package com.vikpix.api.auth.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.request.UpdatePasswordRequest;
import com.vikpix.api.auth.dto.response.UpdatePasswordResponse;
import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.users.entities.User;

@Service
public class UpdatePasswordService {
    private final CurrentUserService currentUserService;
    private final KeycloakAdminClient keycloakAdminClient;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public UpdatePasswordService(
        CurrentUserService currentUserService,
        KeycloakAdminClient keycloakAdminClient,
        PasswordPolicyValidator passwordPolicyValidator
    ) {
        this.currentUserService = currentUserService;
        this.keycloakAdminClient = keycloakAdminClient;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    public UpdatePasswordResponse execute(Authentication authentication, UpdatePasswordRequest request) {
        User user = currentUserService.getAuthenticatedUser(authentication);

        passwordPolicyValidator.validate(request.newPassword());

        if (request.currentPassword().equals(request.newPassword())) {
            throw new RuntimeException("A nova senha deve ser diferente da senha atual");
        }

        try {
            keycloakAdminClient.authenticateUser(user.getEmail(), request.currentPassword(), false);
        } catch (RuntimeException exception) {
            throw new RuntimeException("Senha atual incorreta");
        }
        keycloakAdminClient.resetPassword(user.getKeycloakId(), request.newPassword());

        return new UpdatePasswordResponse(true);
    }
}
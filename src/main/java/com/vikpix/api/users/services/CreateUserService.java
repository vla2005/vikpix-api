package com.vikpix.api.users.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.users.dto.request.CreateUserRequest;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class CreateUserService {
    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    public CreateUserService(UserRepository userRepository, KeycloakAdminClient keycloakAdminClient) {
        this.userRepository = userRepository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Transactional
    public User execute(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.email())) {
            throw new RuntimeException("Email ja cadastrado");
        }

        if (userRepository.existsByUserName(createUserRequest.userName())) {
            throw new RuntimeException("Nome de usuario ja cadastrado");
        }

        String keycloakId = keycloakAdminClient.createUser(createUserRequest);

        User user = User.builder()
            .keycloakId(keycloakId)
            .name(createUserRequest.name())
            .userName(createUserRequest.userName())
            .email(createUserRequest.email())
            .build();

        return userRepository.save(user);
    }
}
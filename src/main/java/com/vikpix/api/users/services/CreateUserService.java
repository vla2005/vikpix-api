package com.vikpix.api.users.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.auth.services.PasswordPolicyValidator;
import com.vikpix.api.donation.services.CreateDefaultDonationConfigsService;
import com.vikpix.api.users.dto.request.CreateUserRequest;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;
import com.vikpix.api.widgets.services.CreateDefaultWidgetsService;

@Service
public class CreateUserService {
    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final CreateDefaultWidgetsService createDefaultWidgetsService;
    private final CreateDefaultDonationConfigsService createDefaultDonationConfigsService;

    public CreateUserService(
        UserRepository userRepository,
        KeycloakAdminClient keycloakAdminClient,
        PasswordPolicyValidator passwordPolicyValidator,
        CreateDefaultWidgetsService createDefaultWidgetsService,
        CreateDefaultDonationConfigsService createDefaultDonationConfigsService
    ) {
        this.userRepository = userRepository;
        this.keycloakAdminClient = keycloakAdminClient;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.createDefaultWidgetsService = createDefaultWidgetsService;
        this.createDefaultDonationConfigsService = createDefaultDonationConfigsService;
    }

    @Transactional
    public User execute(CreateUserRequest createUserRequest) {
        passwordPolicyValidator.validate(createUserRequest.password());
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

        User savedUser = userRepository.save(user);

        createDefaultWidgetsService.execute(savedUser);

        createDefaultDonationConfigsService.execute(savedUser);

        return savedUser;
    }
}
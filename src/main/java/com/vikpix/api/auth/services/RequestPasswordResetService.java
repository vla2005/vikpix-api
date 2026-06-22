package com.vikpix.api.auth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.request.PasswordResetRequest;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class RequestPasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final SendResetPasswordEmailService sendResetPasswordEmailService;
    private final String frontendUrl;

    public RequestPasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenService passwordResetTokenService,
        SendResetPasswordEmailService sendResetPasswordEmailService,
        @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenService = passwordResetTokenService;
        this.sendResetPasswordEmailService = sendResetPasswordEmailService;
        this.frontendUrl = frontendUrl;
    }

    public void execute(PasswordResetRequest request) {
        var userOptional = userRepository.findByEmail(request.email());

        if (userOptional.isEmpty()) {
            return;
        }

        var user = userOptional.get();

        String rawToken = passwordResetTokenService.createToken(user);
        String resetLink = normalizeFrontendUrl() + "/reset-password?token=" + rawToken;

        sendResetPasswordEmailService.sendResetPasswordEmail(user.getEmail(), user.getName(), resetLink);
    }

    private String normalizeFrontendUrl() {
        if (frontendUrl.endsWith("/")) {
            return frontendUrl.substring(0, frontendUrl.length() - 1);
        }

        return frontendUrl;
    }
}
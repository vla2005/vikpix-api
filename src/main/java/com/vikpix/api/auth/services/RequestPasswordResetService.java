package com.vikpix.api.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.dto.request.PasswordResetRequest;
import com.vikpix.api.users.repository.UserRepository;

@Service
public class RequestPasswordResetService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private SendResetPasswordEmailService sendResetPasswordEmailService;

    public void execute(PasswordResetRequest request) {
        var userOptional = userRepository.findByEmail(request.email());

        if (userOptional.isEmpty()) {
            return;
        }

        var user = userOptional.get();

        String rawToken = passwordResetTokenService.createToken(user);
        String resetLink = "http://localhost:5173/reset-password?token=" + rawToken;

        sendResetPasswordEmailService.sendResetPasswordEmail(user.getEmail(), user.getName(), resetLink);
    }
}

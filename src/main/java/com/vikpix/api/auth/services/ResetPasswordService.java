package com.vikpix.api.auth.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vikpix.api.auth.entities.PasswordResetToken;
import com.vikpix.api.auth.keycloak.KeycloakAdminClient;
import com.vikpix.api.auth.repository.PasswordResetTokenRepository;
import com.vikpix.api.users.entities.User;
import com.vikpix.api.users.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ResetPasswordService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private PasswordPolicyValidator passwordPolicyValidator;


    @Transactional
    public void resetPassword(String token, String newPassword) {
        String tokenHash = passwordResetTokenService.hashToken(token);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new RuntimeException("Token invÃ¡lido"));

        if(resetToken.isUsed()) {
            throw new RuntimeException("Token jÃ¡ utilizado");
        }
        
        if(resetToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }

        User user = resetToken.getUser();

        passwordPolicyValidator.validate(newPassword);

        keycloakAdminClient.resetPassword(user.getKeycloakId(), newPassword);
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
    }
}
